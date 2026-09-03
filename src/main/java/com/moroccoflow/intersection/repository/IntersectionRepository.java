package com.moroccoflow.intersection.repository;

import com.moroccoflow.intersection.entity.IntersectionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntersectionRepository extends JpaRepository<IntersectionEntity, Long> {

    @EntityGraph(attributePaths = "city")
    @Override
    List<IntersectionEntity> findAll();

    @EntityGraph(attributePaths = "city")
    @Override
    Optional<IntersectionEntity> findById(Long id);

    @EntityGraph(attributePaths = "city")
    List<IntersectionEntity> findByCityId(Long cityId);

    boolean existsByCityIdAndName(Long cityId, String name);

    boolean existsByCityIdAndNameAndIdNot(Long cityId, String name, Long id);
}