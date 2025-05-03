package com.livedatatrail.backend.domain.events;

import lombok.Getter;
import java.util.Map;

@Getter
public class ItemCreatedEvent extends DomainEvent {
    private final String name;
    private final Double speed;
    private final boolean active;
    private final Map<String, Object> properties;

    public ItemCreatedEvent(String itemId, String name, Double speed, boolean active, Map<String, Object> properties) {
        super(itemId, "ITEM_CREATED");
        this.name = name;
        this.speed = speed;
        this.active = active;
        this.properties = properties;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
} 