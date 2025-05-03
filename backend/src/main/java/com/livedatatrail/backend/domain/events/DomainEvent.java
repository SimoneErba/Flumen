package com.livedatatrail.backend.domain.events;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class DomainEvent {
    private final String eventId;
    private final String entityId;
    private final Instant timestamp;
    private final String eventType;

    protected DomainEvent(String entityId, String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.entityId = entityId;
        this.timestamp = Instant.now();
        this.eventType = eventType;
    }

    // Abstract process method
    public abstract void process();
} 