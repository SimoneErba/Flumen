package com.livedatatrail.backend.domain;

import java.util.Map;

import com.livedatatrail.backend.domain.events.LocationActivatedEvent;
import com.livedatatrail.backend.domain.events.LocationCreatedEvent;
import com.livedatatrail.backend.domain.events.LocationDeactivatedEvent;
import com.livedatatrail.backend.domain.events.LocationPropertiesUpdatedEvent;

import lombok.Getter;

@Getter
public class Location {
    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double length;
    private Double speed;
    private String type;
    private Boolean active;
    private Map<String, Object> properties;

    public Location(
        String id,
        String name,
        Double latitude,
        Double longitude,
        Double length,
        Double speed,
        String type,
        Boolean active,
        Map<String, Object> properties
    ) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.length = length;
        this.speed = speed;
        this.type = type;
        this.active = active;
        this.properties = properties;
    }

    public Location(LocationCreatedEvent event) {
        this.id = event.getEntityId();
        this.name = event.getName();
        this.latitude = event.getLatitude();
        this.longitude = event.getLongitude();
        this.length = event.getLength();
        this.speed = event.getSpeed();
        this.type = event.getType();
        this.active = event.getActive();
        this.properties = event.getProperties();
    }

    public void updateProperties(LocationPropertiesUpdatedEvent event) {
        this.properties = event.getUpdatedProperties();
    }

    public void activate(LocationActivatedEvent event) {
        this.active = true;
    }

    public void deactivate(LocationDeactivatedEvent event) {
        this.active = false;
    }
} 