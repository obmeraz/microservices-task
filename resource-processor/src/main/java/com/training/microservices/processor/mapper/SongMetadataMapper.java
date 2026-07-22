package com.training.microservices.processor.mapper;

import com.training.microservices.processor.dto.SongMetadataRequest;
import com.training.microservices.processor.service.Mp3MetadataExtractor.ExtractedMetadata;
import org.springframework.stereotype.Component;

@Component
public class SongMetadataMapper {

    public SongMetadataRequest toSongMetadataRequest(Long resourceId, ExtractedMetadata metadata) {
        return new SongMetadataRequest(
                resourceId,
                metadata.name(),
                metadata.artist(),
                metadata.album(),
                formatDuration(metadata.durationSeconds()),
                metadata.year()
        );
    }

    private String formatDuration(String durationSeconds) {
        double totalSeconds = Double.parseDouble(durationSeconds.trim());
        int seconds = (int) Math.round(totalSeconds);
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
