package com.training.microservices.song.service.impl;

import com.training.microservices.song.dto.IdResponse;
import com.training.microservices.song.dto.IdsResponse;
import com.training.microservices.song.dto.SongDto;
import com.training.microservices.song.entity.SongEntity;
import com.training.microservices.song.exception.ConflictException;
import com.training.microservices.song.exception.SongNotFoundException;
import com.training.microservices.song.mapper.SongMapper;
import com.training.microservices.song.repository.SongRepository;
import com.training.microservices.song.service.SongService;
import com.training.microservices.song.util.IdValidator;
import com.training.microservices.song.util.IdsParameterParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SongServiceImpl implements SongService {

    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);

    private final SongRepository songRepository;
    private final SongMapper songMapper;

    public SongServiceImpl(SongRepository songRepository, SongMapper songMapper) {
        this.songRepository = songRepository;
        this.songMapper = songMapper;
    }

    @Override
    @Transactional
    public IdResponse create(SongDto songDto) {
        IdValidator.validatePositiveId(songDto.id());

        if (songRepository.existsById(songDto.id())) {
            log.warn("Song metadata already exists for id={}", songDto.id());
            throw new ConflictException("Metadata for resource ID=" + songDto.id() + " already exists");
        }

        SongEntity entity = songMapper.toEntity(songDto);
        songRepository.save(entity);
        log.info("Created song metadata: id={}, name={}, artist={}",
                entity.getId(), songDto.name(), songDto.artist());
        return new IdResponse(entity.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public SongDto getById(Long id) {
        IdValidator.validatePositiveId(id);

        SongEntity entity = songRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Song metadata not found for id={}", id);
                    return new SongNotFoundException("Song metadata for ID=" + id + " not found");
                });
        log.debug("Fetched song metadata: id={}", id);
        return songMapper.toDto(entity);
    }

    @Override
    @Transactional
    public IdsResponse deleteByIds(String idsParameter) {
        List<Long> ids = IdsParameterParser.parse(idsParameter);
        List<Long> deletedIds = new ArrayList<>();

        for (Long id : ids) {
            if (songRepository.existsById(id)) {
                songRepository.deleteById(id);
                deletedIds.add(id);
            } else {
                log.debug("Skip delete for missing song metadata id={}", id);
            }
        }

        log.info("Deleted song metadata: requested={}, deleted={}", ids.size(), deletedIds.size());
        return new IdsResponse(deletedIds);
    }
}
