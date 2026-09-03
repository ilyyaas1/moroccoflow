package com.moroccoflow.traffic.generator;

import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.ConflictException;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.road.entity.RoadEntity;
import com.moroccoflow.road.repository.RoadRepository;
import com.moroccoflow.traffic.generator.dto.SimulatorStartRequest;
import com.moroccoflow.traffic.generator.dto.SimulatorStatusResponse;
import com.moroccoflow.traffic.service.TrafficMeasurementService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class TrafficSimulator {

    static final int DEFAULT_INTERVAL_SECONDS = 5;

    private static final Logger log = LoggerFactory.getLogger(TrafficSimulator.class);

    private final ThreadPoolTaskScheduler trafficSimulatorScheduler;
    private final TrafficMeasurementService trafficMeasurementService;
    private final RoadRepository roadRepository;
    private final CityRepository cityRepository;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean ticking = new AtomicBoolean(false);

    private volatile TrafficScenario scenario = TrafficScenario.NORMAL;
    private volatile int intervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private volatile List<Long> roadIds = List.of();
    private ScheduledFuture<?> job;

    public synchronized SimulatorStatusResponse start(SimulatorStartRequest request) {
        List<Long> resolvedRoadIds = resolveRoadIds(request);
        if (resolvedRoadIds.isEmpty()) {
            throw new ConflictException("No roads available to simulate. Create at least one road first.");
        }

        stopJob();

        this.scenario = request.scenario() != null ? request.scenario() : TrafficScenario.NORMAL;
        this.intervalSeconds = request.intervalSeconds() != null
                ? request.intervalSeconds()
                : DEFAULT_INTERVAL_SECONDS;
        this.roadIds = List.copyOf(resolvedRoadIds);
        this.running.set(true);

        this.job = trafficSimulatorScheduler.scheduleAtFixedRate(
                this::tick,
                Instant.now(),
                Duration.ofSeconds(this.intervalSeconds));

        log.info("Traffic simulator started: scenario={}, interval={}s, roads={}",
                scenario, intervalSeconds, roadIds);
        return status();
    }

    public synchronized SimulatorStatusResponse stop() {
        stopJob();
        running.set(false);
        log.info("Traffic simulator stopped");
        return status();
    }

    public synchronized SimulatorStatusResponse changeScenario(TrafficScenario next) {
        this.scenario = next;
        log.info("Traffic simulator scenario set to {}", next);
        return status();
    }

    public SimulatorStatusResponse status() {
        return new SimulatorStatusResponse(running.get(), scenario, intervalSeconds, roadIds);
    }

    void tick() {
        if (!running.get() || !ticking.compareAndSet(false, true)) {
            return;
        }
        try {
            TrafficScenario currentScenario = this.scenario;
            for (Long roadId : List.copyOf(roadIds)) {
                emitForRoad(roadId, currentScenario);
            }
        } finally {
            ticking.set(false);
        }
    }

    private void emitForRoad(Long roadId, TrafficScenario currentScenario) {
        try {
            RoadEntity road = roadRepository.findById(roadId).orElse(null);
            if (road == null) {
                log.warn("Simulator skipped missing road {}", roadId);
                return;
            }
            trafficMeasurementService.create(
                    SimulatedMeasurementFactory.create(roadId, currentScenario, road.getSpeedLimitKm()));
        } catch (RuntimeException ex) {
            log.warn("Simulator failed to emit measurement for road {}: {}", roadId, ex.getMessage());
        }
    }

    private void stopJob() {
        if (job != null) {
            job.cancel(false);
            job = null;
        }
    }

    private List<Long> resolveRoadIds(SimulatorStartRequest request) {
        if (request.cityId() != null && !cityRepository.existsById(request.cityId())) {
            throw new NotFoundException("City with id %d not found".formatted(request.cityId()));
        }

        if (request.roadIds() != null && !request.roadIds().isEmpty()) {
            LinkedHashSet<Long> unique = new LinkedHashSet<>(request.roadIds());
            List<Long> resolved = new ArrayList<>();
            for (Long roadId : unique) {
                RoadEntity road = roadRepository.findById(roadId)
                        .orElseThrow(() -> new NotFoundException("Road with id %d not found".formatted(roadId)));
                if (request.cityId() != null && !request.cityId().equals(road.getCity().getId())) {
                    throw new ConflictException(
                            "Road %d does not belong to city %d".formatted(roadId, request.cityId()));
                }
                resolved.add(roadId);
            }
            return resolved;
        }

        if (request.cityId() != null) {
            return roadRepository.findByCityId(request.cityId()).stream()
                    .map(RoadEntity::getId)
                    .toList();
        }

        return roadRepository.findAll().stream()
                .map(RoadEntity::getId)
                .toList();
    }
}
