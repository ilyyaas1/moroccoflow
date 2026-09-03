CREATE TABLE traffic_measurements (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    road_id         BIGINT         NOT NULL REFERENCES roads (id) ON DELETE CASCADE,
    measured_at     TIMESTAMPTZ    NOT NULL,
    vehicle_count   INT            NOT NULL CHECK (vehicle_count >= 0),
    average_speed   NUMERIC(6, 2)  NOT NULL CHECK (average_speed >= 0),
    occupancy_rate  NUMERIC(5, 2)  NOT NULL CHECK (occupancy_rate BETWEEN 0 AND 100),
    travel_time     NUMERIC(8, 2)  NOT NULL CHECK (travel_time >= 0),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_traffic_measurements_road_measured_at
    ON traffic_measurements (road_id, measured_at DESC);

CREATE INDEX idx_traffic_measurements_measured_at
    ON traffic_measurements (measured_at DESC);
