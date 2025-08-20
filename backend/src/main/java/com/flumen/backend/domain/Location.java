package com.flumen.backend.domain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import flumen.events.LocationActivatedEvent;
import flumen.events.LocationCreatedEvent;
import flumen.events.LocationDeactivatedEvent;
import flumen.events.LocationPropertiesUpdatedEvent;

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
    private Set<String> outboundConnectionIds = new HashSet<>();

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
        this.outboundConnectionIds = new HashSet<>();
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

    public void addConnectionTo(String toLocationId) {
        if (toLocationId != null && !this.id.equals(toLocationId)) {
            this.outboundConnectionIds.add(toLocationId);
        }
    }

    public void removeConnectionTo(String toLocationId) {
        this.outboundConnectionIds.remove(toLocationId);
    }

    public boolean canMoveTo(String targetLocationId) {
        return this.outboundConnectionIds.contains(targetLocationId);
    }
} 