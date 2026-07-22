package com.training.microservices.processor.client;

import com.training.microservices.processor.dto.SongMetadataRequest;
import com.training.microservices.processor.exception.SongServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SongServiceClient {

    private static final Logger log = LoggerFactory.getLogger(SongServiceClient.class);

    private final RestClient restClient;

    public SongServiceClient(@Qualifier("songServiceRestClient") RestClient songServiceRestClient) {
        this.restClient = songServiceRestClient;
    }

    @Retryable(
            retryFor = {SongServiceException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public void createSongMetadata(SongMetadataRequest request) {
        try {
            restClient.post()
                    .uri("/songs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(), (req, res) ->
                            log.info("Song metadata already exists for id={}", request.id()))
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("Failed to create song metadata via Song Service", ex);
            throw new SongServiceException("Failed to create song metadata", ex);
        }
    }
}
