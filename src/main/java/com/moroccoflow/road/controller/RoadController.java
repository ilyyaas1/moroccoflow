package com.moroccoflow.road.controller;



import com.moroccoflow.road.dto.RoadRequest;
import com.moroccoflow.road.dto.RoadResponse;
import com.moroccoflow.road.service.RoadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roads")
@RequiredArgsConstructor
public class RoadController {

    private final RoadService roadService;

    @GetMapping
    public List<RoadResponse> findAll(@RequestParam(required = false) Long cityId) {
        return cityId == null ? roadService.findAll() : roadService.findByCityId(cityId);
    }

    @GetMapping("/{id}")
    public RoadResponse findById(@PathVariable Long id) {
        return roadService.findById(id);
    }

    @PostMapping
    public ResponseEntity<RoadResponse> create(@Valid @RequestBody RoadRequest request) {
        RoadResponse response = roadService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/roads/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public RoadResponse update(@PathVariable Long id, @Valid @RequestBody RoadRequest request) {
        return roadService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roadService.delete(id);
        return ResponseEntity.noContent().build();
    }
}