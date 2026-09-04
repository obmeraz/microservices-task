package com.training.microservices.processor.consumer;

import com.training.microservices.processor.service.ResourceProcessingService;
import com.training.microservices.processor.trace.TraceId;
import com.training.microservices.processor.trace.TraceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class ResourceUploadedConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResourceUploadedConsumer.class);

    private final ResourceProcessingService resourceProcessingService;

    public ResourceUploadedConsumer(ResourceProcessingService resourceProcessingService) {
        this.resourceProcessingService = resourceProcessingService;
    }

    @Bean
    public Consumer<Message<Long>> resourceUploaded() {
        return message -> {
            String traceId = message.getHeaders().get(TraceId.HEADER, String.class);
            TraceIdContext.set(traceId);
            Long resourceId = message.getPayload();
            log.info("Received resource.uploaded event: resourceId={}", resourceId);
            try {
                resourceProcessingService.process(resourceId);
                log.info("Successfully processed resource.uploaded event: resourceId={}", resourceId);
            } catch (Exception ex) {
                log.error("Failed to process resource.uploaded event: resourceId={}", resourceId, ex);
                throw ex;
            } finally {
                TraceIdContext.clear();
            }
        };
    }
}
