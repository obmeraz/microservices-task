package com.training.microservices.processor.exception;

public class ResourceServiceException extends RuntimeException {

    public ResourceServiceException(String message) {
        super(message);
    }

    public ResourceServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
