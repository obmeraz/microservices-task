package com.training.microservices.resource.messaging;

import com.training.microservices.resource.exception.MessagePublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class ResourceUploadedPublisher {

    private static final Logger log = LoggerFactory.getLogger(ResourceUploadedPublisher.class);

    private final StreamBridge streamBridge;

    public ResourceUploadedPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Retryable(
            retryFor = {MessagePublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public void publish(Long resourceId) {
        log.info("Publishing resource.uploaded event for id={}", resourceId);
        try {
            boolean sent = streamBridge.send("resourceUploaded-out-0", resourceId);
            if (!sent) {
                throw new MessagePublishException(
                        "Failed to publish resource.uploaded event for id=" + resourceId);
            }
        } catch (MessagePublishException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to publish resource.uploaded event for id={}", resourceId, ex);
            throw new MessagePublishException(
                    "Failed to publish resource.uploaded event for id=" + resourceId, ex);
        }
    }
}
