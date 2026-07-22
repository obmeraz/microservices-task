package com.training.microservices.processor.service.impl;

import com.training.microservices.processor.client.ResourceServiceClient;
import com.training.microservices.processor.client.SongServiceClient;
import com.training.microservices.processor.dto.SongMetadataRequest;
import com.training.microservices.processor.exception.ResourceServiceException;
import com.training.microservices.processor.mapper.SongMetadataMapper;
import com.training.microservices.processor.service.Mp3MetadataExtractor;
import com.training.microservices.processor.service.ResourceProcessingService;
import org.springframework.stereotype.Service;

@Service
public class ResourceProcessingServiceImpl implements ResourceProcessingService {

    private final ResourceServiceClient resourceServiceClient;
    private final Mp3MetadataExtractor mp3MetadataExtractor;
    private final SongMetadataMapper songMetadataMapper;
    private final SongServiceClient songServiceClient;

    public ResourceProcessingServiceImpl(ResourceServiceClient resourceServiceClient,
                                         Mp3MetadataExtractor mp3MetadataExtractor,
                                         SongMetadataMapper songMetadataMapper,
                                         SongServiceClient songServiceClient) {
        this.resourceServiceClient = resourceServiceClient;
        this.mp3MetadataExtractor = mp3MetadataExtractor;
        this.songMetadataMapper = songMetadataMapper;
        this.songServiceClient = songServiceClient;
    }

    @Override
    public void process(Long resourceId) {
        byte[] resourceData = resourceServiceClient.getResourceData(resourceId);
        if (resourceData == null) {
            throw new ResourceServiceException("Resource data is not defined for id=" + resourceId);
        }
        Mp3MetadataExtractor.ExtractedMetadata extract = mp3MetadataExtractor.extract(resourceData);
        SongMetadataRequest songMetadataRequest =
                songMetadataMapper.toSongMetadataRequest(resourceId, extract);
        songServiceClient.createSongMetadata(songMetadataRequest);
    }
}
