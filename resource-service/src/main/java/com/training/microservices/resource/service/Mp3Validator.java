package com.training.microservices.resource.service;

import com.training.microservices.resource.exception.BadRequestException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
public class Mp3Validator {

    private final Tika tika = new Tika();

    public void validate(byte[] mp3Data) {
        if (mp3Data == null || mp3Data.length == 0) {
            throw new BadRequestException("Invalid MP3");
        }

        try {
            String mediaType = tika.detect(mp3Data);
            if (mediaType == null || (!mediaType.contains("mpeg") && !mediaType.contains("mp3"))) {
                throw new BadRequestException("Invalid MP3");
            }
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Invalid MP3");
        }
    }
}
