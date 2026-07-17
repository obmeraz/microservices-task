package com.training.microservices.processor.dto;

/**
 * Payload sent from Resource Service to Song Service when creating song metadata.
 */
public record SongMetadataRequest(
        Long id,
        String name,
        String artist,
        String album,
        String duration,
        String year
) {
}
