package com.livedatatrail.backend.domain.events;

import lombok.Getter;

@Getter
public class ItemPositionChangedEvent extends DomainEvent {
    private final String locationId;

    public ItemPositionChangedEvent(String itemId, String locationId) {
        super(itemId, "ITEM_POSITION_CHANGED");
        this.locationId = locationId;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
}
