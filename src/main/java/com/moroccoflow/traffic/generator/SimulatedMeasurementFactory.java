package com.moroccoflow.traffic.generator;

import com.moroccoflow.traffic.dto.TrafficMeasurementRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Builds a plausible measurement from a scenario: volume and speed stay in documented
 * ranges; occupancy rises as speed falls; travel time assumes a 5 km segment.
 */
public final class SimulatedMeasurementFactory {

    static final double SEGMENT_KM = 5.0;

    private SimulatedMeasurementFactory() {
    }

    public static TrafficMeasurementRequest create(Long roadId, TrafficScenario scenario, Integer speedLimitKmh) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int vehicleCount = random.nextInt(scenario.minVehicleCount(), scenario.maxVehicleCount() + 1);
        double speed = random.nextDouble(scenario.minSpeedKmh(), Math.nextUp(scenario.maxSpeedKmh()));
        if (speedLimitKmh != null && speedLimitKmh > 0) {
            speed = Math.min(speed, speedLimitKmh);
        }
        speed = Math.max(speed, 0.1);

        double occupancy = occupancyFromSpeed(speed, scenario);
        double travelTimeMinutes = (SEGMENT_KM / speed) * 60.0;

        return new TrafficMeasurementRequest(
                roadId,
                Instant.now(),
                vehicleCount,
                scale(speed),
                scale(occupancy),
                scale(travelTimeMinutes)
        );
    }

    static double occupancyFromSpeed(double speedKmh, TrafficScenario scenario) {
        double min = scenario.minSpeedKmh();
        double max = scenario.maxSpeedKmh();
        double ratio = max <= min ? 1.0 : (max - speedKmh) / (max - min);
        ratio = Math.clamp(ratio, 0.0, 1.0);
        return scenario.minOccupancy() + (ratio * (scenario.maxOccupancy() - scenario.minOccupancy()));
    }

    private static BigDecimal scale(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
