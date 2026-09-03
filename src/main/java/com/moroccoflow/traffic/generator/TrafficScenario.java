package com.moroccoflow.traffic.generator;

public enum TrafficScenario {
    NORMAL(300, 500, 40, 55, 25, 45),
    RUSH_HOUR(700, 1200, 15, 30, 60, 85),
    ACCIDENT(400, 900, 5, 15, 80, 98),
    ROAD_CLOSURE(0, 80, 1, 8, 5, 25),
    WEATHER(400, 700, 20, 35, 50, 75),
    EVENT(800, 1400, 12, 25, 70, 95);

    private final int minVehicleCount;
    private final int maxVehicleCount;
    private final double minSpeedKmh;
    private final double maxSpeedKmh;
    private final double minOccupancy;
    private final double maxOccupancy;

    TrafficScenario(int minVehicleCount, int maxVehicleCount,
                    double minSpeedKmh, double maxSpeedKmh,
                    double minOccupancy, double maxOccupancy) {
        this.minVehicleCount = minVehicleCount;
        this.maxVehicleCount = maxVehicleCount;
        this.minSpeedKmh = minSpeedKmh;
        this.maxSpeedKmh = maxSpeedKmh;
        this.minOccupancy = minOccupancy;
        this.maxOccupancy = maxOccupancy;
    }

    public int minVehicleCount() {
        return minVehicleCount;
    }

    public int maxVehicleCount() {
        return maxVehicleCount;
    }

    public double minSpeedKmh() {
        return minSpeedKmh;
    }

    public double maxSpeedKmh() {
        return maxSpeedKmh;
    }

    public double minOccupancy() {
        return minOccupancy;
    }

    public double maxOccupancy() {
        return maxOccupancy;
    }
}
