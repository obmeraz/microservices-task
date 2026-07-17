package com.training.microservices.processor.consumer;

import com.training.microservices.processor.service.ResourceProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class ResourceUploadedConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResourceUploadedConsumer.class);

    private final ResourceProcessingService resourceProcessingService;

    public ResourceUploadedConsumer(ResourceProcessingService resourceProcessingService) {
        this.resourceProcessingService = resourceProcessingService;
    }

    @Bean
    public Consumer<Long> resourceUploaded() {
        return resourceId -> {
            log.info("Received resource.uploaded event: resourceId={}", resourceId);
            resourceProcessingService.process(resourceId);
        };
    }
}
