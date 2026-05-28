package com.training.microservices.song.mapper;

import com.training.microservices.song.dto.SongDto;
import com.training.microservices.song.entity.SongEntity;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {

    public SongDto toDto(SongEntity entity) {
        return new SongDto(
                entity.getId(),
                entity.getName(),
                entity.getArtist(),
                entity.getAlbum(),
                entity.getDuration(),
                entity.getYear()
        );
    }

    public SongEntity toEntity(SongDto dto) {
        SongEntity entity = new SongEntity();
        entity.setId(dto.id());
        entity.setName(dto.name());
        entity.setArtist(dto.artist());
        entity.setAlbum(dto.album());
        entity.setDuration(dto.duration());
        entity.setYear(dto.year());
        return entity;
    }
}
