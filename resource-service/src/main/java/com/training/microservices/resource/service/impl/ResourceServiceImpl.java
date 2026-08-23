package com.training.microservices.resource.service.impl;

import com.training.microservices.resource.client.SongServiceClient;
import com.training.microservices.resource.client.StorageServiceClient;
import com.training.microservices.resource.dto.IdResponse;
import com.training.microservices.resource.dto.IdsResponse;
import com.training.microservices.resource.dto.StorageDto;
import com.training.microservices.resource.entity.ResourceEntity;
import com.training.microservices.resource.exception.BadRequestException;
import com.training.microservices.resource.exception.ResourceNotFoundException;
import com.training.microservices.resource.exception.StorageServiceException;
import com.training.microservices.resource.messaging.ResourceUploadedPublisher;
import com.training.microservices.resource.repository.ResourceRepository;
import com.training.microservices.resource.service.Mp3StorageService;
import com.training.microservices.resource.service.Mp3Validator;
import com.training.microservices.resource.service.ResourceService;
import com.training.microservices.resource.util.ContentTypeValidator;
import com.training.microservices.resource.util.IdValidator;
import com.training.microservices.resource.util.IdsParameterParser;
import com.training.microservices.resource.util.StorageKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);

    public static final String STORAGE_TYPE_STAGING = "STAGING";
    public static final String STORAGE_TYPE_PERMANENT = "PERMANENT";

    private final ResourceRepository resourceRepository;
    private final Mp3Validator mp3Validator;
    private final SongServiceClient songServiceClient;
    private final StorageServiceClient storageServiceClient;
    private final Mp3StorageService mp3StorageService;
    private final ResourceUploadedPublisher resourceUploadedPublisher;

    public ResourceServiceImpl(
            ResourceRepository resourceRepository,
            Mp3Validator mp3Validator,
            SongServiceClient songServiceClient,
            StorageServiceClient storageServiceClient,
            Mp3StorageService mp3StorageService,
            ResourceUploadedPublisher resourceUploadedPublisher
    ) {
        this.resourceRepository = resourceRepository;
        this.mp3Validator = mp3Validator;
        this.songServiceClient = songServiceClient;
        this.storageServiceClient = storageServiceClient;
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

        StorageDto stagingStorage = requireStorage(STORAGE_TYPE_STAGING);
        String storageKey = StorageKeyGenerator.generate(stagingStorage.path());

        log.info("Uploading resource to staging: bucket={}, key={}, size={} bytes",
                stagingStorage.bucket(), storageKey, mp3Data.length);
        mp3StorageService.upload(mp3Data, stagingStorage.bucket(), storageKey);

        ResourceEntity entity = new ResourceEntity();
        entity.setStorageKey(storageKey);
        entity.setStorageType(stagingStorage.storageType());
        entity.setBucket(stagingStorage.bucket());
        entity.setPath(stagingStorage.path());
        ResourceEntity saved = resourceRepository.save(entity);

        resourceUploadedPublisher.publish(saved.getId());

        log.info("Resource uploaded successfully: id={}, bucket={}, key={}",
                saved.getId(), saved.getBucket(), saved.getStorageKey());
        return new IdResponse(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getById(Long id) {
        IdValidator.validatePositiveId(id);

        ResourceEntity entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID=" + id + " not found"));

        log.info("Downloading resource: id={}, bucket={}, key={}",
                id, entity.getBucket(), entity.getStorageKey());
        return mp3StorageService.download(entity.getBucket(), entity.getStorageKey());
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
                log.info("Deleting resource: id={}, bucket={}, key={}",
                        id, resourceEntity.getBucket(), resourceEntity.getStorageKey());
                mp3StorageService.remove(resourceEntity.getBucket(), resourceEntity.getStorageKey());
                resourceRepository.deleteById(id);
                deletedIds.add(id);
            } else {
                log.debug("Skip delete for missing resource id={}", id);
            }
        }

        songServiceClient.deleteSongMetadata(deletedIds);
        log.info("Deleted resources: requested={}, deleted={}", ids.size(), deletedIds.size());
        return new IdsResponse(deletedIds);
    }

    @Override
    @Transactional
    public void moveToPermanent(Long id) {
        IdValidator.validatePositiveId(id);

        ResourceEntity entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID=" + id + " not found"));

        if (STORAGE_TYPE_PERMANENT.equals(entity.getStorageType())) {
            log.info("Resource already in permanent storage, skipping move: id={}", id);
            return;
        }

        StorageDto permanentStorage = requireStorage(STORAGE_TYPE_PERMANENT);
        String sourceBucket = entity.getBucket();
        String sourceKey = entity.getStorageKey();
        String targetKey = StorageKeyGenerator.generate(permanentStorage.path());

        log.info("Moving resource to permanent: id={}, from={}/{}, to={}/{}",
                id, sourceBucket, sourceKey, permanentStorage.bucket(), targetKey);
        mp3StorageService.move(sourceBucket, sourceKey, permanentStorage.bucket(), targetKey);

        entity.setStorageType(permanentStorage.storageType());
        entity.setBucket(permanentStorage.bucket());
        entity.setPath(permanentStorage.path());
        entity.setStorageKey(targetKey);
        resourceRepository.save(entity);

        log.info("Resource moved to permanent successfully: id={}, bucket={}, key={}",
                id, entity.getBucket(), entity.getStorageKey());
    }

    private StorageDto requireStorage(String storageType) {
        return storageServiceClient.getStorages().stream()
                .filter(storage -> storageType.equals(storage.storageType()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Storage definition not found for type={}", storageType);
                    return new StorageServiceException(
                            "Storage definition not found for type=" + storageType
                    );
                });
    }
}
