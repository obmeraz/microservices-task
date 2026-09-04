package com.training.microservices.resource.consumer;

import com.training.microservices.resource.service.ResourceService;
import com.training.microservices.resource.trace.TraceId;
import com.training.microservices.resource.trace.TraceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class ResourceProcessedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResourceProcessedEventConsumer.class);

    private final ResourceService resourceService;

    public ResourceProcessedEventConsumer(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Bean
    public Consumer<Message<Long>> resourceProcessed() {
        return message -> {
            String traceId = message.getHeaders().get(TraceId.HEADER, String.class);
            TraceIdContext.set(traceId);
            Long resourceId = message.getPayload();
            log.info("Received resource.processed event: resourceId={}", resourceId);
            try {
                resourceService.moveToPermanent(resourceId);
                log.info("Successfully moved resource to permanent storage: resourceId={}", resourceId);
            } catch (Exception ex) {
                log.error("Failed to process resource.processed event: resourceId={}", resourceId, ex);
                throw ex;
            } finally {
                TraceIdContext.clear();
            }
        };
    }
}
