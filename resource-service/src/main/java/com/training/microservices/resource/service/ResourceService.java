package com.training.microservices.resource.service;

import com.training.microservices.resource.dto.IdResponse;
import com.training.microservices.resource.dto.IdsResponse;

public interface ResourceService {

    IdResponse upload(byte[] mp3Data, String contentType);

    byte[] getById(Long id);

    IdsResponse deleteByIds(String idsParameter);
}
