package com.moroccoflow.city.controller;



import com.moroccoflow.city.dto.CityRequest;
import com.moroccoflow.city.dto.CityResponse;
import com.moroccoflow.city.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public List<CityResponse> findAll() {
        return cityService.findAll();
    }

    @GetMapping("/{id}")
    public CityResponse findById(@PathVariable Long id) {
        return cityService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CityResponse> create(@Valid @RequestBody CityRequest request) {
        CityResponse response = cityService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/cities/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public CityResponse update(@PathVariable Long id, @Valid @RequestBody CityRequest request) {
        return cityService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}