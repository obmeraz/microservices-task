package com.training.microservices.resource.service.impl;

import com.training.microservices.resource.client.SongServiceClient;
import com.training.microservices.resource.dto.IdResponse;
import com.training.microservices.resource.dto.IdsResponse;
import com.training.microservices.resource.dto.SongMetadataRequest;
import com.training.microservices.resource.entity.ResourceEntity;
import com.training.microservices.resource.exception.BadRequestException;
import com.training.microservices.resource.exception.ResourceNotFoundException;
import com.training.microservices.resource.mapper.SongMetadataMapper;
import com.training.microservices.resource.repository.ResourceRepository;
import com.training.microservices.resource.service.Mp3MetadataExtractor;
import com.training.microservices.resource.service.Mp3Validator;
import com.training.microservices.resource.service.ResourceService;
import com.training.microservices.resource.util.ContentTypeValidator;
import com.training.microservices.resource.util.IdValidator;
import com.training.microservices.resource.util.IdsParameterParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final Mp3Validator mp3Validator;
    private final Mp3MetadataExtractor mp3MetadataExtractor;
    private final SongMetadataMapper songMetadataMapper;
    private final SongServiceClient songServiceClient;

    public ResourceServiceImpl(
            ResourceRepository resourceRepository,
            Mp3Validator mp3Validator,
            Mp3MetadataExtractor mp3MetadataExtractor,
            SongMetadataMapper songMetadataMapper,
            SongServiceClient songServiceClient
    ) {
        this.resourceRepository = resourceRepository;
        this.mp3Validator = mp3Validator;
        this.mp3MetadataExtractor = mp3MetadataExtractor;
        this.songMetadataMapper = songMetadataMapper;
        this.songServiceClient = songServiceClient;
    }

    @Override
    @Transactional
    public IdResponse upload(byte[] mp3Data, String contentType) {
        ContentTypeValidator.validateMp3ContentType(contentType);

        if (mp3Data == null || mp3Data.length == 0) {
            throw new BadRequestException("Invalid MP3");
        }

        mp3Validator.validate(mp3Data);
        Mp3MetadataExtractor.ExtractedMetadata extractedMetadata = mp3MetadataExtractor.extract(mp3Data);

        ResourceEntity entity = new ResourceEntity();
        entity.setData(mp3Data);
        ResourceEntity saved = resourceRepository.save(entity);

        SongMetadataRequest songMetadataRequest =
                songMetadataMapper.toSongMetadataRequest(saved.getId(), extractedMetadata);
        songServiceClient.createSongMetadata(songMetadataRequest);

        return new IdResponse(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getById(Long id) {
        IdValidator.validatePositiveId(id);

        ResourceEntity entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID=" + id + " not found"));
        return entity.getData();
    }

    @Override
    @Transactional
    public IdsResponse deleteByIds(String idsParameter) {
        List<Long> ids = IdsParameterParser.parse(idsParameter);
        List<Long> deletedIds = new ArrayList<>();

        for (Long id : ids) {
            if (resourceRepository.existsById(id)) {
                resourceRepository.deleteById(id);
                deletedIds.add(id);
            }
        }

        songServiceClient.deleteSongMetadata(deletedIds);
        return new IdsResponse(deletedIds);
    }
}
