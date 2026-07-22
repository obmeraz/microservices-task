package com.training.microservices.resource.service.impl;

import com.training.microservices.resource.client.SongServiceClient;
import com.training.microservices.resource.dto.IdResponse;
import com.training.microservices.resource.dto.IdsResponse;
import com.training.microservices.resource.entity.ResourceEntity;
import com.training.microservices.resource.exception.BadRequestException;
import com.training.microservices.resource.exception.ResourceNotFoundException;
import com.training.microservices.resource.messaging.ResourceUploadedPublisher;
import com.training.microservices.resource.repository.ResourceRepository;
import com.training.microservices.resource.service.Mp3StorageService;
import com.training.microservices.resource.service.Mp3Validator;
import com.training.microservices.resource.service.ResourceService;
import com.training.microservices.resource.util.ContentTypeValidator;
import com.training.microservices.resource.util.IdValidator;
import com.training.microservices.resource.util.IdsParameterParser;
import com.training.microservices.resource.util.StorageKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final Mp3Validator mp3Validator;
    private final SongServiceClient songServiceClient;
    private final Mp3StorageService mp3StorageService;
    private final ResourceUploadedPublisher resourceUploadedPublisher;

    public ResourceServiceImpl(
            ResourceRepository resourceRepository,
            Mp3Validator mp3Validator,
            SongServiceClient songServiceClient,
            Mp3StorageService mp3StorageService,
            ResourceUploadedPublisher resourceUploadedPublisher
    ) {
        this.resourceRepository = resourceRepository;
        this.mp3Validator = mp3Validator;
        this.songServiceClient = songServiceClient;
        this.mp3StorageService = mp3StorageService;
        this.resourceUploadedPublisher = resourceUploadedPublisher;
    }

    @Override
    @Transactional
    public IdResponse upload(byte[] mp3Data, String contentType) {
        ContentTypeValidator.validateMp3ContentType(contentType);

        if (mp3Data == null || mp3Data.length == 0) {
            throw new BadRequestException("Invalid MP3");
        }

        mp3Validator.validate(mp3Data);

        String storageKey = StorageKeyGenerator.generate();
        ResourceEntity entity = new ResourceEntity();
        entity.setStorageKey(storageKey);
        ResourceEntity saved = resourceRepository.save(entity);
        mp3StorageService.upload(mp3Data, storageKey);

        resourceUploadedPublisher.publish(saved.getId());

        return new IdResponse(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getById(Long id) {
        IdValidator.validatePositiveId(id);

        ResourceEntity entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID=" + id + " not found"));
        return mp3StorageService.download(entity.getStorageKey());
    }

    @Override
    @Transactional
    public IdsResponse deleteByIds(String idsParameter) {
        List<Long> ids = IdsParameterParser.parse(idsParameter);
        List<Long> deletedIds = new ArrayList<>();

        for (Long id : ids) {
            Optional<ResourceEntity> entity = resourceRepository.findById(id);
            if (entity.isPresent()) {
                ResourceEntity resourceEntity = entity.get();
                String storageKey = resourceEntity.getStorageKey();
                mp3StorageService.remove(storageKey);
                resourceRepository.deleteById(id);
                deletedIds.add(id);
            }
        }

        songServiceClient.deleteSongMetadata(deletedIds);
        return new IdsResponse(deletedIds);
    }
}
