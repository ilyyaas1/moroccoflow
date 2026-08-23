CREATE TABLE cities (
                        id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        name        VARCHAR(120)  NOT NULL,
                        country     VARCHAR(60)   NOT NULL DEFAULT 'Morocco',
                        latitude    NUMERIC(9, 6) NOT NULL,
                        longitude   NUMERIC(9, 6) NOT NULL,
                        created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
                        updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
                        CONSTRAINT chk_city_coords CHECK (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180)
);

CREATE TABLE roads (
                       id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       city_id        BIGINT        NOT NULL REFERENCES cities (id) ON DELETE CASCADE,
                       name           VARCHAR(150)  NOT NULL,
                       road_type      VARCHAR(20)   NOT NULL,
                       speed_limit_km INT           NOT NULL CHECK (speed_limit_km BETWEEN 5 AND 130),
                       lanes          INT           NOT NULL DEFAULT 1 CHECK (lanes >= 1),
                       geometry       JSONB,
                       created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
                       updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
                       CONSTRAINT uq_road_city_name UNIQUE (city_id, name),
                       CONSTRAINT chk_road_type CHECK (road_type IN ('HIGHWAY', 'ARTERIAL', 'COLLECTOR', 'LOCAL'))
);

CREATE TABLE intersections (
                               id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               city_id    BIGINT        NOT NULL REFERENCES cities (id) ON DELETE CASCADE,
                               name       VARCHAR(150)  NOT NULL,
                               latitude   NUMERIC(9, 6) NOT NULL,
                               longitude  NUMERIC(9, 6) NOT NULL,
                               created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
                               updated_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
                               CONSTRAINT uq_intersection_city_name UNIQUE (city_id, name),
                               CONSTRAINT chk_intersection_coords CHECK (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180)
);