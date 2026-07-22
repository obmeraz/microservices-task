package com.training.microservices.resource.exception;

public class MessagePublishException extends RuntimeException {

    public MessagePublishException(String message) {
        super(message);
    }

    public MessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
