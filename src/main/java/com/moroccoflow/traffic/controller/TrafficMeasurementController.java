package com.moroccoflow.traffic.controller;

import com.moroccoflow.traffic.dto.TrafficMeasurementRequest;
import com.moroccoflow.traffic.dto.TrafficMeasurementResponse;
import com.moroccoflow.traffic.service.TrafficMeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/traffic")
@RequiredArgsConstructor
public class TrafficMeasurementController {

    private final TrafficMeasurementService trafficMeasurementService;

    @GetMapping("/measurements")
    public List<TrafficMeasurementResponse> findAll(
            @RequestParam(required = false) Long roadId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer limit) {
        return trafficMeasurementService.findAll(roadId, cityId, from, to, limit);
    }

    @PostMapping("/measurements")
    public ResponseEntity<TrafficMeasurementResponse> create(
            @Valid @RequestBody TrafficMeasurementRequest request) {
        TrafficMeasurementResponse response = trafficMeasurementService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/traffic/measurements/" + response.id()))
                .body(response);
    }

    @GetMapping("/roads/{roadId}")
    public List<TrafficMeasurementResponse> findByRoadId(
            @PathVariable Long roadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer limit) {
        return trafficMeasurementService.findByRoadId(roadId, from, to, limit);
    }

    @GetMapping("/roads/{roadId}/history")
    public List<TrafficMeasurementResponse> findHistory(
            @PathVariable Long roadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer limit) {
        return trafficMeasurementService.findByRoadId(roadId, from, to, limit);
    }

    @GetMapping("/roads/{roadId}/latest")
    public TrafficMeasurementResponse findLatest(@PathVariable Long roadId) {
        return trafficMeasurementService.findLatestByRoadId(roadId);
    }
}
