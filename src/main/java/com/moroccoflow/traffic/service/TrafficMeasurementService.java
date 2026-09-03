package com.moroccoflow.traffic.service;

import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.road.entity.RoadEntity;
import com.moroccoflow.road.repository.RoadRepository;
import com.moroccoflow.traffic.dto.TrafficMeasurementRequest;
import com.moroccoflow.traffic.dto.TrafficMeasurementResponse;
import com.moroccoflow.traffic.entity.TrafficMeasurementEntity;
import com.moroccoflow.traffic.mapper.TrafficMeasurementMapper;
import com.moroccoflow.traffic.repository.TrafficMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrafficMeasurementService {

    static final int DEFAULT_LIMIT = 200;
    static final int MAX_LIMIT = 1000;

    private final TrafficMeasurementRepository measurementRepository;
    private final RoadRepository roadRepository;
    private final CityRepository cityRepository;

    public List<TrafficMeasurementResponse> findAll(
            Long roadId, Long cityId, Instant from, Instant to, Integer limit) {
        if (roadId != null) {
            assertRoadExists(roadId);
        }
        if (cityId != null) {
            assertCityExists(cityId);
        }
        return search(roadId, cityId, from, to, limit);
    }

    public List<TrafficMeasurementResponse> findByRoadId(
            Long roadId, Instant from, Instant to, Integer limit) {
        assertRoadExists(roadId);
        return search(roadId, null, from, to, limit);
    }

    public TrafficMeasurementResponse findLatestByRoadId(Long roadId) {
        return findLatestOptional(roadId)
                .orElseThrow(() -> new NotFoundException(
                        "No traffic measurement found for road %d".formatted(roadId)));
    }

    public Optional<TrafficMeasurementResponse> findLatestOptional(Long roadId) {
        assertRoadExists(roadId);
        return measurementRepository.findTopByRoadIdOrderByTimestampDesc(roadId)
                .map(TrafficMeasurementMapper::toResponse);
    }

    @Transactional
    public TrafficMeasurementResponse create(TrafficMeasurementRequest request) {
        RoadEntity road = roadRepository.findById(request.roadId())
                .orElseThrow(() -> new NotFoundException(
                        "Road with id %d not found".formatted(request.roadId())));
        TrafficMeasurementEntity entity = TrafficMeasurementMapper.toEntity(request, road);
        return TrafficMeasurementMapper.toResponse(measurementRepository.save(entity));
    }

    private List<TrafficMeasurementResponse> search(
            Long roadId, Long cityId, Instant from, Instant to, Integer limit) {
        int size = resolveLimit(limit);
        return measurementRepository
                .search(roadId, cityId, from, to, PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "timestamp")))
                .getContent()
                .stream()
                .map(TrafficMeasurementMapper::toResponse)
                .toList();
    }

    private void assertRoadExists(Long roadId) {
        if (!roadRepository.existsById(roadId)) {
            throw new NotFoundException("Road with id %d not found".formatted(roadId));
        }
    }

    private void assertCityExists(Long cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new NotFoundException("City with id %d not found".formatted(cityId));
        }
    }

    private static int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.clamp(limit, 1, MAX_LIMIT);
    }
}
