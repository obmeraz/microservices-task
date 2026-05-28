package com.training.microservices.song.service;

import com.training.microservices.song.dto.IdResponse;
import com.training.microservices.song.dto.IdsResponse;
import com.training.microservices.song.dto.SongDto;

public interface SongService {

    IdResponse create(SongDto songDto);

    SongDto getById(Long id);

    IdsResponse deleteByIds(String idsParameter);
}
