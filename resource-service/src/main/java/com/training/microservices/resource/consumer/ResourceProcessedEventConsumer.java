package com.training.microservices.resource.consumer;

import com.training.microservices.resource.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class ResourceProcessedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResourceProcessedEventConsumer.class);

    private final ResourceService resourceService;

    public ResourceProcessedEventConsumer(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Bean
    public Consumer<Long> resourceProcessed() {
        return resourceId -> {
            log.info("Received resource.processed event: resourceId={}", resourceId);
            resourceService.moveToPermanent(resourceId);
        };
    }
}
