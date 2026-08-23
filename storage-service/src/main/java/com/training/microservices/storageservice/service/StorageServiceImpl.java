package com.training.microservices.storageservice.service;

import com.training.microservices.storageservice.dto.IdResponse;
import com.training.microservices.storageservice.dto.IdsResponse;
import com.training.microservices.storageservice.dto.StorageDto;
import com.training.microservices.storageservice.entity.StorageEntity;
import com.training.microservices.storageservice.mapper.StorageMapper;
import com.training.microservices.storageservice.repository.StorageRepository;
import com.training.microservices.storageservice.util.IdsParameterParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    private final StorageRepository storageRepository;
    private final StorageMapper storageMapper;

    public StorageServiceImpl(StorageRepository storageRepository, StorageMapper storageMapper) {
        this.storageRepository = storageRepository;
        this.storageMapper = storageMapper;
    }

    @Override
    @Transactional
    public IdResponse create(StorageDto storageDto) {
        StorageEntity entity = storageMapper.toEntity(storageDto);
        StorageEntity saved = storageRepository.save(entity);
        log.info("Created storage: id={}, type={}, bucket={}, path={}",
                saved.getId(), storageDto.storageType(), storageDto.bucket(), storageDto.path());
        return new IdResponse(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageDto> getAll() {
        List<StorageDto> storages = storageRepository.findAll().stream()
                .map(storageMapper::toDto)
                .toList();
        log.debug("Fetched storages: count={}", storages.size());
        return storages;
    }

    @Override
    @Transactional
    public IdsResponse deleteByIds(String idsParameter) {
        List<Long> ids = IdsParameterParser.parse(idsParameter);
        List<Long> deletedIds = new ArrayList<>();

        for (Long id : ids) {
            if (storageRepository.existsById(id)) {
                storageRepository.deleteById(id);
                deletedIds.add(id);
            } else {
                log.debug("Skip delete for missing storage id={}", id);
            }
        }

        log.info("Deleted storages: requested={}, deleted={}", ids.size(), deletedIds.size());
        return new IdsResponse(deletedIds);
    }
}
