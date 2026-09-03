package com.moroccoflow.traffic.congestion;

import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.road.entity.RoadEntity;
import com.moroccoflow.road.entity.RoadEntity.RoadType;
import com.moroccoflow.road.repository.RoadRepository;
import com.moroccoflow.traffic.congestion.dto.CongestionResponse;
import com.moroccoflow.traffic.dto.TrafficMeasurementResponse;
import com.moroccoflow.traffic.service.TrafficMeasurementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CongestionServiceTest {

    @Mock
    private TrafficMeasurementService trafficMeasurementService;

    @Mock
    private RoadRepository roadRepository;

    @Mock
    private CityRepository cityRepository;

    private CongestionService congestionService;

    @BeforeEach
    void setUp() {
        congestionService = new CongestionService(
                new CongestionCalculator(new CongestionProperties()),
                trafficMeasurementService,
                roadRepository,
                cityRepository);
    }

    @Test
    void forRoad_unknownRoad_throwsNotFound() {
        when(roadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> congestionService.forRoad(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Road with id 99 not found");
    }

    @Test
    void forRoad_returnsCalculatedSnapshot() {
        Instant at = Instant.parse("2026-08-23T08:30:00Z");
        when(roadRepository.findById(12L)).thenReturn(Optional.of(sampleRoad()));
        when(trafficMeasurementService.findLatestByRoadId(12L)).thenReturn(
                new TrafficMeasurementResponse(40L, 12L, 1L, at, 532,
                        new BigDecimal("28.4"), new BigDecimal("67.2"), new BigDecimal("8.5"), at));

        CongestionResponse result = congestionService.forRoad(12L);

        assertThat(result.roadId()).isEqualTo(12L);
        assertThat(result.level()).isEqualTo(CongestionLevel.MODERATE);
        assertThat(result.speedLimitKm()).isEqualTo(50);
    }

    @Test
    void findCurrent_skipsRoadsWithoutMeasurements() {
        when(cityRepository.existsById(1L)).thenReturn(true);
        when(roadRepository.findByCityId(1L)).thenReturn(List.of(sampleRoad()));
        when(trafficMeasurementService.findLatestOptional(12L)).thenReturn(Optional.empty());

        assertThat(congestionService.findCurrent(1L)).isEmpty();
    }

    @Test
    void findCurrent_unknownCity_throwsNotFound() {
        when(cityRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> congestionService.findCurrent(9L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("City with id 9 not found");
    }

    private static RoadEntity sampleRoad() {
        return new RoadEntity(12L, null, "Avenue Mohammed V", RoadType.ARTERIAL,
                50, 2, null, null, null);
    }
}
