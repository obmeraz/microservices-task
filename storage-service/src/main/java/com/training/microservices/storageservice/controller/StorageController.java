package com.training.microservices.storageservice.controller;

import com.training.microservices.storageservice.dto.IdResponse;
import com.training.microservices.storageservice.dto.IdsResponse;
import com.training.microservices.storageservice.dto.StorageDto;
import com.training.microservices.storageservice.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/storages")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<IdResponse> create(@Valid @RequestBody StorageDto storageDto) {
        return ResponseEntity.ok(storageService.create(storageDto));
    }

    @GetMapping
    public ResponseEntity<List<StorageDto>> getAll() {
        return ResponseEntity.ok(storageService.getAll());
    }

    @DeleteMapping
    public ResponseEntity<IdsResponse> delete(@RequestParam("id") String ids) {
        return ResponseEntity.ok(storageService.deleteByIds(ids));
    }
}
