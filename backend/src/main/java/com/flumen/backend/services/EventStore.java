package com.flumen.backend.services;

import flumen.events.DomainEvent;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventStore {
    
    private final OrientDBService orientDBService;

    @Autowired
    public EventStore(OrientDBService orientDBService) {
        this.orientDBService = orientDBService;
        initializeEventStore();
    }

    private void initializeEventStore() {
        orientDBService.withSession(session -> {
            if (session.getClass("Event") == null) {
                session.createVertexClass("Event");
            }
        });
    }

    public void saveEvents(String aggregateId, List<DomainEvent> events) {
        orientDBService.withSession(session -> {
            for (DomainEvent event : events) {
                saveEvent(session, event);
            }
        });
    }

    private void saveEvent(ODatabaseSession session, DomainEvent event) {
        OVertex eventVertex = session.newVertex("Event");
        eventVertex.setProperty("eventId", event.getEventId());
        eventVertex.setProperty("entityId", event.getEntityId());
        eventVertex.setProperty("timestamp", event.getTimestamp());
        eventVertex.setProperty("eventType", event.getEventType());
        eventVertex.setProperty("eventData", event);
        eventVertex.save();
    }

    public List<DomainEvent> getEventsForEntity(String entityId) {
        List<DomainEvent> events = new ArrayList<>();
        orientDBService.withSession(session -> {
            String query = "SELECT FROM Event WHERE entityId = ? ORDER BY timestamp ASC";
            session.query(query, entityId).stream().forEach(result -> {
                OVertex vertex = result.getVertex().get();
                events.add((DomainEvent) vertex.getProperty("eventData"));
            });
        });
        return events;
    }
} 