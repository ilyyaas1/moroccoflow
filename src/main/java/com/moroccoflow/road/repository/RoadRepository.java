package com.moroccoflow.road.repository;

import com.moroccoflow.road.entity.RoadEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadRepository extends JpaRepository<RoadEntity, Long> {

    @EntityGraph(attributePaths = "city")
    @Override
    List<RoadEntity> findAll();

    @EntityGraph(attributePaths = "city")
    @Override
    Optional<RoadEntity> findById(Long id);

    @EntityGraph(attributePaths = "city")
    List<RoadEntity> findByCityId(Long cityId);

    boolean existsByCityIdAndName(Long cityId, String name);

    boolean existsByCityIdAndNameAndIdNot(Long cityId, String name, Long id);
}
