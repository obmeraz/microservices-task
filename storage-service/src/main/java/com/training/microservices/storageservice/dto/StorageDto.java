package com.training.microservices.storageservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StorageDto(
        Long id,

        @NotBlank(message = "Storage type is required")
        @Pattern(regexp = "STAGING|PERMANENT", message = "Storage type must be STAGING or PERMANENT")
        String storageType,

        @NotBlank(message = "Bucket is required")
        @Size(max = 100, message = "Bucket must be at most 100 characters")
        String bucket,

        @NotBlank(message = "Path is required")
        @Size(max = 100, message = "Path must be at most 100 characters")
        String path
) {
}
