package com.training.microservices.resource.controller;

import com.training.microservices.resource.dto.IdResponse;
import com.training.microservices.resource.dto.IdsResponse;
import com.training.microservices.resource.service.ResourceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<IdResponse> upload(
            @RequestBody(required = false) byte[] mp3Data,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType
    ) {
        IdResponse response = resourceService.upload(mp3Data, contentType);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getById(@PathVariable Long id) {
        byte[] audio = resourceService.getById(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .body(audio);
    }

    @DeleteMapping
    public ResponseEntity<IdsResponse> delete(@RequestParam("id") String ids) {
        IdsResponse response = resourceService.deleteByIds(ids);
        return ResponseEntity.ok(response);
    }
}
