package com.training.microservices.resource.dto;

public record StorageDto(
        Long id,

        String storageType,

        String bucket,

        String path
) {
}
