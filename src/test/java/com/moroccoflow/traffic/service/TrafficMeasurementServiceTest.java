package com.moroccoflow.traffic.service;

import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.road.entity.RoadEntity;
import com.moroccoflow.road.entity.RoadEntity.RoadType;
import com.moroccoflow.road.repository.RoadRepository;
import com.moroccoflow.traffic.dto.TrafficMeasurementRequest;
import com.moroccoflow.traffic.dto.TrafficMeasurementResponse;
import com.moroccoflow.traffic.entity.TrafficMeasurementEntity;
import com.moroccoflow.traffic.repository.TrafficMeasurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrafficMeasurementServiceTest {

    private static final Instant T1 = Instant.parse("2026-08-23T08:30:00Z");
    private static final Instant T2 = Instant.parse("2026-08-23T08:45:00Z");

    @Mock
    private TrafficMeasurementRepository measurementRepository;

    @Mock
    private RoadRepository roadRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private TrafficMeasurementService trafficMeasurementService;

    @Test
    void create_unknownRoad_throwsNotFound() {
        when(roadRepository.findById(12L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trafficMeasurementService.create(sampleRequest(T1)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Road with id 12 not found");
        verify(measurementRepository, never()).save(any());
    }

    @Test
    void create_savesMeasurement() {
        when(roadRepository.findById(12L)).thenReturn(Optional.of(sampleRoad()));
        when(measurementRepository.save(any(TrafficMeasurementEntity.class))).thenAnswer(inv -> {
            TrafficMeasurementEntity entity = inv.getArgument(0);
            entity.setId(40L);
            return entity;
        });

        TrafficMeasurementResponse result = trafficMeasurementService.create(sampleRequest(T1));

        assertThat(result.id()).isEqualTo(40L);
        assertThat(result.roadId()).isEqualTo(12L);
        assertThat(result.cityId()).isEqualTo(1L);
        assertThat(result.vehicleCount()).isEqualTo(532);
        assertThat(result.timestamp()).isEqualTo(T1);
    }

    @Test
    void findLatest_returnsNewestRow() {
        when(roadRepository.existsById(12L)).thenReturn(true);
        when(measurementRepository.findTopByRoadIdOrderByTimestampDesc(12L))
                .thenReturn(Optional.of(sampleMeasurement(40L, T2)));

        TrafficMeasurementResponse result = trafficMeasurementService.findLatestByRoadId(12L);

        assertThat(result.id()).isEqualTo(40L);
        assertThat(result.timestamp()).isEqualTo(T2);
    }

    @Test
    void findLatest_noRows_throwsNotFound() {
        when(roadRepository.existsById(12L)).thenReturn(true);
        when(measurementRepository.findTopByRoadIdOrderByTimestampDesc(12L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trafficMeasurementService.findLatestByRoadId(12L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No traffic measurement found for road 12");
    }

    @Test
    void findByRoadId_unknownRoad_throwsNotFound() {
        when(roadRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> trafficMeasurementService.findByRoadId(999L, null, null, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Road with id 999 not found");
    }

    @Test
    void findAll_filtersByCityAndRange() {
        when(cityRepository.existsById(1L)).thenReturn(true);
        when(measurementRepository.search(eq(null), eq(1L), eq(T1), eq(T2), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleMeasurement(40L, T1))));

        List<TrafficMeasurementResponse> result =
                trafficMeasurementService.findAll(null, 1L, T1, T2, 50);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().cityId()).isEqualTo(1L);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(measurementRepository).search(eq(null), eq(1L), eq(T1), eq(T2), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(50);
        assertThat(pageable.getValue().getSort().getOrderFor("timestamp").isDescending()).isTrue();
    }

    @Test
    void findAll_unknownCity_throwsNotFound() {
        when(cityRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> trafficMeasurementService.findAll(null, 9L, null, null, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("City with id 9 not found");
    }

    @Test
    void findAll_clampsLimitToMax() {
        when(measurementRepository.search(eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        trafficMeasurementService.findAll(null, null, null, null, 50_000);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(measurementRepository).search(eq(null), eq(null), eq(null), eq(null), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(TrafficMeasurementService.MAX_LIMIT);
    }

    private static TrafficMeasurementRequest sampleRequest(Instant timestamp) {
        return new TrafficMeasurementRequest(
                12L,
                timestamp,
                532,
                new BigDecimal("28.4"),
                new BigDecimal("67.2"),
                new BigDecimal("8.5"));
    }

    private static TrafficMeasurementEntity sampleMeasurement(Long id, Instant timestamp) {
        return new TrafficMeasurementEntity(
                id,
                sampleRoad(),
                timestamp,
                532,
                new BigDecimal("28.4"),
                new BigDecimal("67.2"),
                new BigDecimal("8.5"),
                timestamp);
    }

    private static RoadEntity sampleRoad() {
        CityEntity city = new CityEntity(1L, "Casablanca", "Morocco",
                new BigDecimal("33.573110"), new BigDecimal("-7.589843"), null, null);
        return new RoadEntity(12L, city, "Avenue Mohammed V", RoadType.ARTERIAL,
                50, 2, null, null, null);
    }
}
