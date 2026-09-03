package com.moroccoflow.traffic.congestion;

import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.road.entity.RoadEntity;
import com.moroccoflow.road.repository.RoadRepository;
import com.moroccoflow.traffic.congestion.dto.CongestionResponse;
import com.moroccoflow.traffic.dto.TrafficMeasurementResponse;
import com.moroccoflow.traffic.service.TrafficMeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CongestionService {

    private final CongestionCalculator congestionCalculator;
    private final TrafficMeasurementService trafficMeasurementService;
    private final RoadRepository roadRepository;
    private final CityRepository cityRepository;

    public CongestionResponse forRoad(Long roadId) {
        RoadEntity road = roadRepository.findById(roadId)
                .orElseThrow(() -> new NotFoundException("Road with id %d not found".formatted(roadId)));
        TrafficMeasurementResponse latest = trafficMeasurementService.findLatestByRoadId(roadId);
        return congestionCalculator.calculate(latest, road.getSpeedLimitKm());
    }

    public List<CongestionResponse> findCurrent(Long cityId) {
        List<RoadEntity> roads;
        if (cityId != null) {
            if (!cityRepository.existsById(cityId)) {
                throw new NotFoundException("City with id %d not found".formatted(cityId));
            }
            roads = roadRepository.findByCityId(cityId);
        } else {
            roads = roadRepository.findAll();
        }

        List<CongestionResponse> results = new ArrayList<>();
        for (RoadEntity road : roads) {
            trafficMeasurementService.findLatestOptional(road.getId())
                    .map(latest -> congestionCalculator.calculate(latest, road.getSpeedLimitKm()))
                    .ifPresent(results::add);
        }
        return results;
    }
}
