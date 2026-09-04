package com.training.microservices.resource.service;

import com.training.microservices.resource.exception.BadRequestException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Mp3Validator {

    private static final Logger log = LoggerFactory.getLogger(Mp3Validator.class);

    private final Tika tika;

    public Mp3Validator() {
        this(new Tika());
    }

    Mp3Validator(Tika tika) {
        this.tika = tika;
    }

    public void validate(byte[] mp3Data) {
        if (mp3Data == null || mp3Data.length == 0) {
            log.warn("MP3 validation failed: empty payload");
            throw new BadRequestException("Invalid MP3");
        }

        try {
            String mediaType = tika.detect(mp3Data);
            if (mediaType == null || (!mediaType.contains("mpeg") && !mediaType.contains("mp3"))) {
                log.warn("MP3 validation failed: unsupported mediaType={}", mediaType);
                throw new BadRequestException("Invalid MP3");
            }
            log.debug("MP3 validation passed: mediaType={}, size={}", mediaType, mp3Data.length);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("MP3 validation failed due to detection error", ex);
            throw new BadRequestException("Invalid MP3");
        }
    }
}
