package com.training.microservices.resource.client;

import com.training.microservices.resource.dto.StorageDto;
import com.training.microservices.resource.exception.StorageServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class StorageServiceClient {

    private static final Logger log = LoggerFactory.getLogger(StorageServiceClient.class);

    private static final ParameterizedTypeReference<List<StorageDto>> STORAGE_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public StorageServiceClient(@Qualifier("storageServiceRestClient") RestClient storageServiceRestClient) {
        this.restClient = storageServiceRestClient;
    }

    @CircuitBreaker(name = "storageService", fallbackMethod = "getStoragesFallback")
    public List<StorageDto> getStorages() {
        try {
            List<StorageDto> storages = restClient.get()
                    .uri("/storages")
                    .retrieve()
                    .body(STORAGE_LIST_TYPE);
            return storages != null ? storages : List.of();
        } catch (RestClientException ex) {
            log.error("Failed to get storages from Storage Service", ex);
            throw new StorageServiceException("Failed to get storages", ex);
        }
    }

    private List<StorageDto> getStoragesFallback(Throwable ex) {
        log.warn("Circuit breaker fallback for Storage Service. Returning stub storages. cause={}",
                ex.toString());
        return StorageServiceFallback.stubStorages();
    }
}
