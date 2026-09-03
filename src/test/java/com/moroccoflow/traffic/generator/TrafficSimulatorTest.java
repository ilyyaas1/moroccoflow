package com.moroccoflow.traffic.generator;

import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.ConflictException;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.road.entity.RoadEntity;
import com.moroccoflow.road.entity.RoadEntity.RoadType;
import com.moroccoflow.road.repository.RoadRepository;
import com.moroccoflow.traffic.dto.TrafficMeasurementRequest;
import com.moroccoflow.traffic.generator.dto.SimulatorStartRequest;
import com.moroccoflow.traffic.generator.dto.SimulatorStatusResponse;
import com.moroccoflow.traffic.service.TrafficMeasurementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrafficSimulatorTest {

    @Mock
    private ThreadPoolTaskScheduler trafficSimulatorScheduler;

    @Mock
    private TrafficMeasurementService trafficMeasurementService;

    @Mock
    private RoadRepository roadRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ScheduledFuture<Object> job;

    @InjectMocks
    private TrafficSimulator trafficSimulator;

    @Test
    void start_noRoads_throwsConflict() {
        when(roadRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> trafficSimulator.start(new SimulatorStartRequest(null, null, null, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("No roads available");
        verify(trafficSimulatorScheduler, never()).scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any());
    }

    @Test
    void start_unknownRoad_throwsNotFound() {
        when(roadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trafficSimulator.start(
                new SimulatorStartRequest(TrafficScenario.NORMAL, 5, null, List.of(99L))))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Road with id 99 not found");
    }

    @Test
    void start_schedulesJobAndReportsStatus() {
        when(roadRepository.findById(12L)).thenReturn(Optional.of(sampleRoad()));
        when(trafficSimulatorScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenAnswer(invocation -> job);

        SimulatorStatusResponse status = trafficSimulator.start(
                new SimulatorStartRequest(TrafficScenario.RUSH_HOUR, 3, null, List.of(12L)));

        assertThat(status.running()).isTrue();
        assertThat(status.scenario()).isEqualTo(TrafficScenario.RUSH_HOUR);
        assertThat(status.intervalSeconds()).isEqualTo(3);
        assertThat(status.roadIds()).containsExactly(12L);
        verify(trafficSimulatorScheduler).scheduleAtFixedRate(any(Runnable.class), any(Instant.class), eq(Duration.ofSeconds(3)));
    }

    @Test
    void tick_writesMeasurementForEachRoad() {
        when(roadRepository.findById(12L)).thenReturn(Optional.of(sampleRoad()));
        when(trafficSimulatorScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenAnswer(invocation -> job);
        trafficSimulator.start(new SimulatorStartRequest(TrafficScenario.NORMAL, 5, null, List.of(12L)));

        trafficSimulator.tick();

        verify(trafficMeasurementService).create(any(TrafficMeasurementRequest.class));
    }

    @Test
    void changeScenario_updatesWithoutRestart() {
        trafficSimulator.changeScenario(TrafficScenario.ACCIDENT);

        assertThat(trafficSimulator.status().scenario()).isEqualTo(TrafficScenario.ACCIDENT);
        verify(trafficSimulatorScheduler, never()).scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any());
    }

    @Test
    void stop_cancelsJob() {
        when(roadRepository.findById(12L)).thenReturn(Optional.of(sampleRoad()));
        when(trafficSimulatorScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenAnswer(invocation -> job);
        trafficSimulator.start(new SimulatorStartRequest(null, null, null, List.of(12L)));

        SimulatorStatusResponse status = trafficSimulator.stop();

        assertThat(status.running()).isFalse();
        verify(job).cancel(false);
    }

    private static RoadEntity sampleRoad() {
        CityEntity city = new CityEntity(1L, "Casablanca", "Morocco",
                new BigDecimal("33.573110"), new BigDecimal("-7.589843"), null, null);
        return new RoadEntity(12L, city, "Avenue Mohammed V", RoadType.ARTERIAL,
                50, 2, null, null, null);
    }
}
