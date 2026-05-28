package com.training.microservices.song.exception;

import org.springframework.http.HttpStatus;

public class SongNotFoundException extends ApiException {

    public SongNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
