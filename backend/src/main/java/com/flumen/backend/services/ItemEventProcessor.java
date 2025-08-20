package com.flumen.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flumen.backend.domain.Item;
import com.flumen.backend.domain.Location;
import flumen.events.DomainEvent;
import flumen.events.ItemActivatedEvent;
import flumen.events.ItemCreatedEvent;
import flumen.events.ItemDeactivatedEvent;
import flumen.events.ItemPositionChangedEvent;
import flumen.events.ItemPropertiesUpdatedEvent;
import flumen.events.ItemSpeedChangedEvent;
import flumen.events.LocationConnectionCreatedEvent;
import flumen.events.LocationCreatedEvent;
import flumen.events.LocationPropertiesUpdatedEvent;
import com.flumen.backend.models.UpdateModel;
import com.flumen.backend.models.input.ItemInput;
import com.flumen.backend.models.input.LocationInput;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class ItemEventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ItemEventProcessor.class);

    private final EventStore eventStore;
    private final OrientDBService orientDBService;
    private final ClickHouseService clickHouseService;
    private final ItemService itemService;
    private final LocationService locationService;
    private final WebSocketService webSocketService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ItemEventProcessor(
            EventStore eventStore, 
            OrientDBService orientDBService,
            ClickHouseService clickHouseService,
            ItemService itemService,
            LocationService locationService,
            WebSocketService webSocketService) {
        this.eventStore = eventStore;
        this.orientDBService = orientDBService;
        this.clickHouseService = clickHouseService;
        this.itemService = itemService;
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    public CompletableFuture<Void> process(DomainEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 1. Store the event in OrientDB
                eventStore.saveEvents(event.getEntityId(), List.of(event));
                
                // 2. Store the event in ClickHouse
                clickHouseService.saveEvent(event);
                
                // 3. Update the read model using existing services
                processEvent(event);
                
                logger.info("Successfully processed event: {} for item: {}", 
                    event.getEventType(), event.getEntityId());
            } catch (Exception e) {
                logger.error("Error processing event: {} for item: {}", 
                    event.getEventType(), event.getEntityId(), e);
                throw e;
            }
        });
    }

/**
 * A generic helper that executes a database operation and retries it
 * automatically if a OConcurrentModificationException occurs.
 *
 * @param operation The block of code to execute.
 */
private void executeWithRetry(Runnable operation) {
    final int MAX_RETRIES = 3;
    int attempt = 0;

    while (true) { // Loop indefinitely until success or permanent failure
        try {
            operation.run();
            return; // Operation was successful, exit the method.
        } catch (OConcurrentModificationException e) {
            attempt++;
            if (attempt >= MAX_RETRIES) {
                logger.error("Operation failed after {} retries due to persistent concurrent modification.", MAX_RETRIES, e);
                throw e; // Give up and re-throw the exception.
            }

            logger.warn("Concurrent modification detected. Retrying attempt {}/{}.", attempt, MAX_RETRIES);
            
            try {
                Thread.sleep(50 + new Random().nextInt(50)); // Wait before next attempt
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry attempt was interrupted", ie);
            }
        }
    }
}

   private void processEvent(DomainEvent event) {
    executeWithRetry(() -> {
        switch (event) {
            case ItemCreatedEvent e -> {
                var item = new ItemInput(e);
                itemService.createItem(item);
            }

            case ItemPositionChangedEvent e -> {
                var item = itemService.getItemById(e.getEntityId());
                var location = locationService.getLocationById(e.getLocationId());
                item.updatePosition(location);
                itemService.fullUpdateItem(item);
                webSocketService.broadcastPositionUpdate(item.getId(), location.getId());
            }

            case ItemSpeedChangedEvent e -> {
                var item = itemService.getItemById(e.getEntityId());
                item.updateSpeed(e);
                itemService.fullUpdateItem(item);
            }

            case ItemDeactivatedEvent e -> { // Corrected type
                var item = itemService.getItemById(e.getEntityId());
                item.stop();
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("active", item.isActive());
                itemService.updateItem(new UpdateModel(item.getId(), updateData));
            }

            case ItemActivatedEvent e -> {
                var item = itemService.getItemById(e.getEntityId());
                item.resume();
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("active", item.isActive());
                itemService.updateItem(new UpdateModel(item.getId(), updateData));
            }

            case ItemPropertiesUpdatedEvent e -> {
                var item = itemService.getItemById(e.getEntityId());
                item.updateProperties(e);
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("properties", item.getProperties());
                itemService.updateItem(new UpdateModel(item.getId(), updateData));
            }

            case LocationCreatedEvent e -> {
                var location = new LocationInput(e);
                locationService.createLocation(location);
            }

            case LocationPropertiesUpdatedEvent e -> {
                var location = locationService.getLocationById(e.getEntityId());
                location.updateProperties(e);
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("properties", location.getProperties());
                locationService.updateLocation(new UpdateModel(location.getId(), updateData));
            }

            case LocationConnectionCreatedEvent e -> {
                var fromLocation = locationService.getLocationById(e.getEntityId());

                fromLocation.addConnectionTo(e.getLocation2Id());

                locationService.fullUpdateLocation(fromLocation);
            }

            default -> logger.warn("Unknown event type: {}", event.getClass().getSimpleName());
            }
        });
    }
} 