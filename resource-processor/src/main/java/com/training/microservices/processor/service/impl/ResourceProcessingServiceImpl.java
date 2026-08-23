package com.training.microservices.processor.service.impl;

import com.training.microservices.processor.client.ResourceServiceClient;
import com.training.microservices.processor.client.SongServiceClient;
import com.training.microservices.processor.dto.SongMetadataRequest;
import com.training.microservices.processor.exception.ResourceServiceException;
import com.training.microservices.processor.mapper.SongMetadataMapper;
import com.training.microservices.processor.publisher.ResourceProcessedEventPublisher;
import com.training.microservices.processor.service.Mp3MetadataExtractor;
import com.training.microservices.processor.service.ResourceProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ResourceProcessingServiceImpl implements ResourceProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ResourceProcessingServiceImpl.class);

    private final ResourceServiceClient resourceServiceClient;
    private final Mp3MetadataExtractor mp3MetadataExtractor;
    private final SongMetadataMapper songMetadataMapper;
    private final SongServiceClient songServiceClient;
    private final ResourceProcessedEventPublisher resourceProcessedEventPublisher;

    public ResourceProcessingServiceImpl(ResourceServiceClient resourceServiceClient,
                                         Mp3MetadataExtractor mp3MetadataExtractor,
                                         SongMetadataMapper songMetadataMapper,
                                         SongServiceClient songServiceClient,
                                         ResourceProcessedEventPublisher resourceProcessedEventPublisher) {
        this.resourceServiceClient = resourceServiceClient;
        this.mp3MetadataExtractor = mp3MetadataExtractor;
        this.songMetadataMapper = songMetadataMapper;
        this.songServiceClient = songServiceClient;
        this.resourceProcessedEventPublisher = resourceProcessedEventPublisher;
    }

    @Override
    public void process(Long resourceId) {
        log.info("Starting resource processing: resourceId={}", resourceId);

        byte[] resourceData = resourceServiceClient.getResourceData(resourceId);
        if (resourceData == null) {
            log.error("Resource data is null for id={}", resourceId);
            throw new ResourceServiceException("Resource data is not defined for id=" + resourceId);
        }

        log.info("Fetched resource data: resourceId={}, size={} bytes", resourceId, resourceData.length);

        Mp3MetadataExtractor.ExtractedMetadata extract = mp3MetadataExtractor.extract(resourceData);
        log.info("Extracted metadata: resourceId={}, name={}, artist={}, album={}, year={}",
                resourceId, extract.name(), extract.artist(), extract.album(), extract.year());

        SongMetadataRequest songMetadataRequest =
                songMetadataMapper.toSongMetadataRequest(resourceId, extract);
        songServiceClient.createSongMetadata(songMetadataRequest);
        resourceProcessedEventPublisher.publish(resourceId);

        log.info("Resource processing completed: resourceId={}", resourceId);
    }
}
