package com.livedatatrail.backend.domain.events;

import lombok.Getter;
import java.util.Map;

@Getter
public class ItemPropertiesUpdatedEvent extends DomainEvent {
    private final Map<String, Object> properties;

    public ItemPropertiesUpdatedEvent(String itemId, Map<String, Object> properties) {
        super(itemId, "ITEM_PROPERTIES_UPDATED");
        this.properties = properties;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
} 