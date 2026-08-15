package com.training.microservices.storageservice.service;

import com.training.microservices.storageservice.dto.IdResponse;
import com.training.microservices.storageservice.dto.IdsResponse;
import com.training.microservices.storageservice.dto.StorageDto;
import com.training.microservices.storageservice.entity.StorageEntity;
import com.training.microservices.storageservice.mapper.StorageMapper;
import com.training.microservices.storageservice.repository.StorageRepository;
import com.training.microservices.storageservice.util.IdsParameterParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StorageServiceImpl implements StorageService {

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
        return new IdResponse(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageDto> getAll() {
        return storageRepository.findAll().stream()
                .map(storageMapper::toDto)
                .toList();
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
            }
        }

        return new IdsResponse(deletedIds);
    }
}
