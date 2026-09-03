package com.moroccoflow.traffic.generator;

import com.moroccoflow.traffic.dto.TrafficMeasurementRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedMeasurementFactoryTest {

    @ParameterizedTest
    @EnumSource(TrafficScenario.class)
    void create_staysInsideScenarioRanges(TrafficScenario scenario) {
        for (int i = 0; i < 80; i++) {
            TrafficMeasurementRequest request = SimulatedMeasurementFactory.create(12L, scenario, 130);

            assertThat(request.roadId()).isEqualTo(12L);
            assertThat(request.timestamp()).isNotNull();
            assertThat(request.vehicleCount())
                    .isBetween(scenario.minVehicleCount(), scenario.maxVehicleCount());
            assertThat(request.averageSpeed().doubleValue())
                    .isBetween(scenario.minSpeedKmh() - 0.05, scenario.maxSpeedKmh() + 0.05);
            assertThat(request.occupancyRate().doubleValue())
                    .isBetween(scenario.minOccupancy() - 0.05, scenario.maxOccupancy() + 0.05);
            assertThat(request.travelTime()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Test
    void create_capsSpeedAtRoadLimit() {
        TrafficMeasurementRequest request =
                SimulatedMeasurementFactory.create(12L, TrafficScenario.NORMAL, 30);

        assertThat(request.averageSpeed().doubleValue()).isLessThanOrEqualTo(30.0);
    }

    @Test
    void occupancy_increasesWhenSpeedDrops() {
        double slow = SimulatedMeasurementFactory.occupancyFromSpeed(15.0, TrafficScenario.RUSH_HOUR);
        double fast = SimulatedMeasurementFactory.occupancyFromSpeed(30.0, TrafficScenario.RUSH_HOUR);

        assertThat(slow).isGreaterThan(fast);
    }
}
