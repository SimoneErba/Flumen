package com.livedatatrail.backend.domain.events;

import lombok.Getter;
import java.util.Map;

@Getter
public class LocationPropertiesUpdatedEvent extends DomainEvent {
    private final Map<String, Object> updatedProperties;

    public LocationPropertiesUpdatedEvent(String locationId, Map<String, Object> updatedProperties) {
        super(locationId, "LOCATION_UPDATED");
        this.updatedProperties = updatedProperties;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
} 