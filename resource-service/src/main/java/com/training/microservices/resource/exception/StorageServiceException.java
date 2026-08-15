package com.training.microservices.resource.exception;

import org.springframework.http.HttpStatus;

public class StorageServiceException extends ApiException {

    public StorageServiceException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public StorageServiceException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
}
