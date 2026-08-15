package com.training.microservices.processor.publisher;


import com.training.microservices.processor.exception.MessagePublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class ResourceProcessedEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ResourceProcessedEventPublisher.class);

    private final StreamBridge streamBridge;

    public ResourceProcessedEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Retryable(
            retryFor = {MessagePublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public void publish(Long resourceId) {
        log.info("Publishing resource.processed event for id={}", resourceId);
        try {
            boolean sent = streamBridge.send("resourceProcessed-out-0", resourceId);
            if (!sent) {
                throw new MessagePublishException(
                        "Failed to publish resource.processed event for id=" + resourceId);
            }
        } catch (MessagePublishException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to publish resource.processed event for id={}", resourceId, ex);
            throw new MessagePublishException(
                    "Failed to publish resource.processed event for id=" + resourceId, ex);
        }
    }
}
