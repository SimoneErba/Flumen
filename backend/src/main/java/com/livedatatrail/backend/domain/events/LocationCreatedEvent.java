package com.livedatatrail.backend.domain.events;

import java.util.Map;

import lombok.Getter;

@Getter
public class LocationCreatedEvent extends DomainEvent {
    private final String name;
    private final Boolean active;
    private Double latitude;
    private Double longitude;
    private Double length;
    private Double speed;
    private String type;
    private final Map<String, Object> properties;

    public LocationCreatedEvent(String locationId, String name, Boolean active, Double latitude, Double longitude, Double length, Double speed, String type, Map<String, Object> properties) {
        super(locationId, "LOCATION_CREATED");
        this.name = name;
        this.active = active;
        this.properties = properties;
        this.latitude = latitude;
        this.longitude = longitude;
        this.length = length;
        this.speed = speed;
        this.type = type;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
} 