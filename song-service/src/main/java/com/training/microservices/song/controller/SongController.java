package com.training.microservices.song.controller;

import com.training.microservices.song.dto.IdResponse;
import com.training.microservices.song.dto.IdsResponse;
import com.training.microservices.song.dto.SongDto;
import com.training.microservices.song.service.SongService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping
    public ResponseEntity<IdResponse> create(@Valid @RequestBody SongDto songDto) {
        IdResponse response = songService.create(songDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getById(@PathVariable Long id) {
        SongDto song = songService.getById(id);
        return ResponseEntity.ok(song);
    }

    @DeleteMapping
    public ResponseEntity<IdsResponse> delete(@RequestParam("id") String ids) {
        IdsResponse response = songService.deleteByIds(ids);
        return ResponseEntity.ok(response);
    }
}
