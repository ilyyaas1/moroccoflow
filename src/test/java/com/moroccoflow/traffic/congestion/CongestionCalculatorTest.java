package com.moroccoflow.traffic.congestion;

import com.moroccoflow.traffic.congestion.dto.CongestionResponse;
import com.moroccoflow.traffic.dto.TrafficMeasurementResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class CongestionCalculatorTest {

    private CongestionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CongestionCalculator(new CongestionProperties());
    }

    @Test
    void exampleMeasurement_isModerate() {
        // speed 28.4 / limit 50 → speedFactor 0.432; occupancy 67.2% → 0.672
        // index = 0.6*0.432 + 0.4*0.672 = 0.528
        CongestionResponse result = calculator.calculate(measurement(
                new BigDecimal("28.4"), new BigDecimal("67.2")), 50);

        assertThat(result.congestionIndex().doubleValue()).isCloseTo(0.5280, within(0.0002));
        assertThat(result.level()).isEqualTo(CongestionLevel.MODERATE);
        assertThat(result.speedLimitKm()).isEqualTo(50);
        assertThat(result.roadId()).isEqualTo(12L);
    }

    @Test
    void freeFlow_isLow() {
        CongestionResponse result = calculator.calculate(measurement(
                new BigDecimal("50"), new BigDecimal("10")), 50);

        assertThat(result.level()).isEqualTo(CongestionLevel.LOW);
        assertThat(result.congestionIndex().doubleValue()).isCloseTo(0.04, within(0.0002));
    }

    @Test
    void speedAboveLimit_doesNotGoNegative() {
        CongestionResponse result = calculator.calculate(measurement(
                new BigDecimal("80"), new BigDecimal("0")), 50);

        assertThat(result.congestionIndex()).isEqualByComparingTo("0.0000");
        assertThat(result.level()).isEqualTo(CongestionLevel.LOW);
    }

    @Test
    void standstillHighOccupancy_isCritical() {
        CongestionResponse result = calculator.calculate(measurement(
                new BigDecimal("2"), new BigDecimal("95")), 50);

        assertThat(result.level()).isEqualTo(CongestionLevel.CRITICAL);
        assertThat(result.congestionIndex().doubleValue()).isGreaterThanOrEqualTo(0.80);
    }

    @Test
    void toLevel_usesInclusiveThresholds() {
        assertThat(calculator.toLevel(0.29)).isEqualTo(CongestionLevel.LOW);
        assertThat(calculator.toLevel(0.30)).isEqualTo(CongestionLevel.MODERATE);
        assertThat(calculator.toLevel(0.60)).isEqualTo(CongestionLevel.HIGH);
        assertThat(calculator.toLevel(0.80)).isEqualTo(CongestionLevel.CRITICAL);
    }

    @Test
    void invalidThresholds_failFast() {
        CongestionProperties properties = new CongestionProperties();
        properties.setModerateThreshold(0.7);
        properties.setHighThreshold(0.5);

        assertThatThrownBy(() -> new CongestionCalculator(properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("moderate < high < critical");
    }

    private static TrafficMeasurementResponse measurement(BigDecimal speed, BigDecimal occupancy) {
        Instant at = Instant.parse("2026-08-23T08:30:00Z");
        return new TrafficMeasurementResponse(
                40L, 12L, 1L, at, 532, speed, occupancy, new BigDecimal("8.5"), at);
    }
}
