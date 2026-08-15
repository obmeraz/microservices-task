package com.training.microservices.storageservice.mapper;

import com.training.microservices.storageservice.dto.StorageDto;
import com.training.microservices.storageservice.entity.StorageEntity;
import org.springframework.stereotype.Component;

@Component
public class StorageMapper {

    public StorageDto toDto(StorageEntity entity) {
        return new StorageDto(
                entity.getId(),
                entity.getStorageType(),
                entity.getBucket(),
                entity.getPath()
        );
    }

    public StorageEntity toEntity(StorageDto dto) {
        StorageEntity entity = new StorageEntity();
        entity.setStorageType(dto.storageType());
        entity.setBucket(dto.bucket());
        entity.setPath(dto.path());
        return entity;
    }
}
