package com.livedatatrail.backend.domain.events;

import lombok.Getter;

@Getter
public class ItemSpeedChangedEvent extends DomainEvent {
    private final Double speed;

    public ItemSpeedChangedEvent(String itemId, Double speed) {
        super(itemId, "ITEM_SPEED_CHANGED");
        this.speed = speed;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
} 