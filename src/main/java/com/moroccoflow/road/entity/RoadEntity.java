package com.moroccoflow.road.entity;

import com.moroccoflow.city.entity.CityEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "roads", uniqueConstraints = {
        @UniqueConstraint(name = "uq_road_city_name", columnNames = {"city_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "road_type", nullable = false, length = 20)
    private RoadType roadType;

    @Column(name = "speed_limit_km", nullable = false)
    private Integer speedLimitKm;

    @Column(nullable = false)
    private Integer lanes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> geometry;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum RoadType {
        HIGHWAY, ARTERIAL, COLLECTOR, LOCAL
    }
}
