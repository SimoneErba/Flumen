package com.livedatatrail.backend.domain.events;

import lombok.Getter;

@Getter
public class LocationActivatedEvent extends DomainEvent {
    public LocationActivatedEvent(String locationId) {
        super(locationId, "LOCATION_ACTIVATED");
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
} 