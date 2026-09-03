package com.moroccoflow.intersection.controller;

import com.moroccoflow.intersection.dto.IntersectionRequest;
import com.moroccoflow.intersection.dto.IntersectionResponse;
import com.moroccoflow.intersection.service.IntersectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/intersections")
@RequiredArgsConstructor
public class IntersectionController {

    private final IntersectionService intersectionService;

    @GetMapping
    public List<IntersectionResponse> findAll() {
        return intersectionService.findAll();
    }

    @GetMapping("/{id}")
    public IntersectionResponse findById(@PathVariable Long id) {
        return intersectionService.findById(id);
    }

    @GetMapping(params = "cityId")
    public List<IntersectionResponse> findByCityId(@RequestParam Long cityId) {
        return intersectionService.findByCityId(cityId);
    }

    @PostMapping
    public ResponseEntity<IntersectionResponse> create(@Valid @RequestBody IntersectionRequest request) {
        IntersectionResponse response = intersectionService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/intersections/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public IntersectionResponse update(@PathVariable Long id, @Valid @RequestBody IntersectionRequest request) {
        return intersectionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        intersectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
