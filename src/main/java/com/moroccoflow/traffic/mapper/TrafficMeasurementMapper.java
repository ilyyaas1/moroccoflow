package com.moroccoflow.traffic.mapper;

import com.moroccoflow.road.entity.RoadEntity;
import com.moroccoflow.traffic.dto.TrafficMeasurementRequest;
import com.moroccoflow.traffic.dto.TrafficMeasurementResponse;
import com.moroccoflow.traffic.entity.TrafficMeasurementEntity;

public final class TrafficMeasurementMapper {

    private TrafficMeasurementMapper() {
    }

    public static TrafficMeasurementEntity toEntity(TrafficMeasurementRequest request, RoadEntity road) {
        TrafficMeasurementEntity entity = new TrafficMeasurementEntity();
        entity.setRoad(road);
        entity.setTimestamp(request.timestamp());
        entity.setVehicleCount(request.vehicleCount());
        entity.setAverageSpeed(request.averageSpeed());
        entity.setOccupancyRate(request.occupancyRate());
        entity.setTravelTime(request.travelTime());
        return entity;
    }

    public static TrafficMeasurementResponse toResponse(TrafficMeasurementEntity entity) {
        return new TrafficMeasurementResponse(
                entity.getId(),
                entity.getRoad().getId(),
                entity.getRoad().getCity().getId(),
                entity.getTimestamp(),
                entity.getVehicleCount(),
                entity.getAverageSpeed(),
                entity.getOccupancyRate(),
                entity.getTravelTime(),
                entity.getCreatedAt()
        );
    }
}
