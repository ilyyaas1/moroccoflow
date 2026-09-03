package com.moroccoflow.traffic.repository;

import com.moroccoflow.traffic.entity.TrafficMeasurementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface TrafficMeasurementRepository extends JpaRepository<TrafficMeasurementEntity, Long> {

    @EntityGraph(attributePaths = {"road", "road.city"})
    Optional<TrafficMeasurementEntity> findTopByRoadIdOrderByTimestampDesc(Long roadId);

    @Query(value = """
            SELECT m FROM TrafficMeasurementEntity m
            JOIN FETCH m.road r
            JOIN FETCH r.city c
            WHERE (:roadId IS NULL OR r.id = :roadId)
              AND (:cityId IS NULL OR c.id = :cityId)
              AND (:fromTs IS NULL OR m.timestamp >= :fromTs)
              AND (:toTs IS NULL OR m.timestamp <= :toTs)
            """,
            countQuery = """
            SELECT COUNT(m) FROM TrafficMeasurementEntity m
            JOIN m.road r
            WHERE (:roadId IS NULL OR r.id = :roadId)
              AND (:cityId IS NULL OR r.city.id = :cityId)
              AND (:fromTs IS NULL OR m.timestamp >= :fromTs)
              AND (:toTs IS NULL OR m.timestamp <= :toTs)
            """)
    Page<TrafficMeasurementEntity> search(
            @Param("roadId") Long roadId,
            @Param("cityId") Long cityId,
            @Param("fromTs") Instant fromTs,
            @Param("toTs") Instant toTs,
            Pageable pageable);
}
