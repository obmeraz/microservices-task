package com.training.microservices.resource.exception;

import org.springframework.http.HttpStatus;

public class SongServiceException extends ApiException {

    public SongServiceException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
