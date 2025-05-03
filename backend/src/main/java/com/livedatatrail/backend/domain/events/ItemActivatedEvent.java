package com.livedatatrail.backend.domain.events;

import lombok.Getter;

@Getter
public class ItemActivatedEvent extends DomainEvent {
    public ItemActivatedEvent(String itemId) {
        super(itemId, "ITEM_ACTIVATED");
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
} 