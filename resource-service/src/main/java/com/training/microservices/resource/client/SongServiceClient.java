package com.training.microservices.resource.client;

import com.training.microservices.resource.dto.SongMetadataRequest;
import com.training.microservices.resource.exception.SongServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SongServiceClient {

    private static final Logger log = LoggerFactory.getLogger(SongServiceClient.class);

    private final RestClient restClient;

    public SongServiceClient(RestClient songServiceRestClient) {
        this.restClient = songServiceRestClient;
    }

    public void createSongMetadata(SongMetadataRequest request) {
        try {
            restClient.post()
                    .uri("/songs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("Failed to create song metadata via Song Service", ex);
            throw new SongServiceException("Failed to create song metadata");
        }
    }

    public void deleteSongMetadata(List<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }

        String idsParameter = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        try {
            restClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/songs")
                            .queryParam("id", idsParameter)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("Failed to delete song metadata via Song Service", ex);
            throw new SongServiceException("Failed to delete song metadata");
        }
    }
}
