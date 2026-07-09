package com.training.microservices.resource.util;

import com.training.microservices.resource.exception.ApiException;
import com.training.microservices.resource.exception.ResourceNotFoundException;
import com.training.microservices.resource.exception.StorageException;
import software.amazon.awssdk.services.s3.model.S3Exception;

public final class S3ExceptionMapper {

    private S3ExceptionMapper() {
    }

    public static ApiException map(S3Exception exception, String operation) {
        String errorCode = exception.awsErrorDetails().errorCode();
        if ("NoSuchKey".equals(errorCode)) {
            return new ResourceNotFoundException("Resource file not found in storage");
        }

        String message = exception.awsErrorDetails().errorMessage();
        return new StorageException("Failed to " + operation + " MP3 in storage: " + message);
    }
}
