package com.training.microservices.storageservice.service;

import com.training.microservices.storageservice.dto.IdResponse;
import com.training.microservices.storageservice.dto.IdsResponse;
import com.training.microservices.storageservice.dto.StorageDto;

import java.util.List;

public interface StorageService {

    IdResponse create(StorageDto storageDto);

    List<StorageDto> getAll();

    IdsResponse deleteByIds(String idsParameter);
}
