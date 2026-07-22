package com.training.microservices.processor.client;

import com.training.microservices.processor.exception.ResourceServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ResourceServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceClient.class);

    private final RestClient restClient;

    public ResourceServiceClient(@Qualifier("resourceServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Retryable(
            retryFor = {ResourceServiceException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public byte[] getResourceData(Long resourceId) {
        try {
            return restClient.get()
                    .uri("/resources/{id}", resourceId)
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException ex) {
            log.error("Failed to get resource data for id={}", resourceId, ex);
            throw new ResourceServiceException("Failed to get resource data for id=" + resourceId, ex);
        }
    }
}
