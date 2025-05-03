package com.livedatatrail.backend.domain;

import com.livedatatrail.backend.domain.events.DomainEvent;
import com.livedatatrail.backend.domain.events.ItemCreatedEvent;
import com.livedatatrail.backend.domain.events.ItemPositionChangedEvent;
import com.livedatatrail.backend.domain.events.ItemPropertiesUpdatedEvent;
import com.livedatatrail.backend.domain.events.ItemSpeedChangedEvent;
import com.livedatatrail.backend.domain.Location;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Item {
    private final String id;
    private String name;
    private Double speed;
    private boolean active;
    private Map<String, Object> properties;
    private Location location;
    private ProgressInfo progressInfo;

    public Item(String id, String name, Double speed, boolean active, Map<String, Object> properties, Location location) {
        this.id = id;
        this.name = name;
        this.speed = speed;
        this.active = active;
        this.properties = properties;
        this.location = location;
    }

    public Item(String id, String name, Double speed, boolean active, Map<String, Object> properties) {
        this.id = id;
        this.name = name;
        this.speed = speed;
        this.active = active;
        this.properties = properties;
    }

    public  Item (ItemCreatedEvent event) {
        this.id = event.getEntityId();
        this.name = event.getName();
        this.speed = event.getSpeed();
        this.active = event.isActive();
        this.properties = event.getProperties();
    }

    public void resume() {
        if (!this.active) {
            this.active = true;
        }
    }

    public void stop() {
        if (this.active) {
            this.active = false;
        }
    }

    public void updatePosition(Location location) {
        this.location = location;
    }

    public void updateSpeed(ItemSpeedChangedEvent event) {
        Duration timeDelta = Duration.between(this.progressInfo.getDatetime(), event.getTimestamp());
        double milliSecondsElapsed = timeDelta.toMillis();
        
        double progressDelta = milliSecondsElapsed * this.speed / this.location.getLength();
        double newProgress = this.progressInfo.getProgress() + progressDelta;
        
        this.progressInfo = new ProgressInfo(newProgress, event.getTimestamp());
        
        this.speed = event.getSpeed();
    }

    public void updateProperties(ItemPropertiesUpdatedEvent event) {
        if (event.getProperties() != null) {
            if (this.properties == null) {
                this.properties = new HashMap<>();
            }
            this.properties.putAll(event.getProperties());
        }
    }    
} 