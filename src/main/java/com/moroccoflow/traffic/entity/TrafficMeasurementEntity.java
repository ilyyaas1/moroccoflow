package com.moroccoflow.traffic.entity;

import com.moroccoflow.road.entity.RoadEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "traffic_measurements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrafficMeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "road_id", nullable = false)
    private RoadEntity road;

    @Column(name = "measured_at", nullable = false)
    private Instant timestamp;

    @Column(name = "vehicle_count", nullable = false)
    private Integer vehicleCount;

    @Column(name = "average_speed", nullable = false, precision = 6, scale = 2)
    private BigDecimal averageSpeed;

    @Column(name = "occupancy_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal occupancyRate;

    @Column(name = "travel_time", nullable = false, precision = 8, scale = 2)
    private BigDecimal travelTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }
}
