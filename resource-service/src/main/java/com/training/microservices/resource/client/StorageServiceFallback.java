package com.training.microservices.resource.client;

import com.training.microservices.resource.dto.StorageDto;

import java.util.List;

public final class StorageServiceFallback {

    private StorageServiceFallback() {
    }

    public static List<StorageDto> stubStorages() {
        return List.of(
                new StorageDto(1L, "STAGING", "staging-storage", "/files"),
                new StorageDto(2L, "PERMANENT", "permanent-storage", "/files")
        );
    }
}
