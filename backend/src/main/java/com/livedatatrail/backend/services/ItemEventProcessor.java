package com.livedatatrail.backend.services;

import com.livedatatrail.backend.domain.Item;
import com.livedatatrail.backend.domain.Location;
import com.livedatatrail.backend.domain.events.DomainEvent;
import com.livedatatrail.backend.domain.events.ItemPropertiesUpdatedEvent;
import com.livedatatrail.backend.domain.events.ItemCreatedEvent;
import com.livedatatrail.backend.domain.events.ItemPositionChangedEvent;
import com.livedatatrail.backend.domain.events.ItemSpeedChangedEvent;
import com.livedatatrail.backend.domain.events.LocationCreatedEvent;
import com.livedatatrail.backend.domain.events.LocationPropertiesUpdatedEvent;
import com.livedatatrail.backend.models.UpdateModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ItemEventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ItemEventProcessor.class);

    private final EventStore eventStore;
    private final OrientDBService orientDBService;
    private final ClickHouseService clickHouseService;
    private final ItemService itemService;
    private final LocationService locationService;

    public ItemEventProcessor(
            EventStore eventStore, 
            OrientDBService orientDBService,
            ClickHouseService clickHouseService,
            ItemService itemService,
            LocationService locationService) {
        this.eventStore = eventStore;
        this.orientDBService = orientDBService;
        this.clickHouseService = clickHouseService;
        this.itemService = itemService;
        this.locationService = locationService;
    }

    public CompletableFuture<Void> process(DomainEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 1. Store the event in OrientDB
                eventStore.saveEvents(event.getEntityId(), List.of(event));
                
                // 2. Store the event in ClickHouse
                clickHouseService.saveEvent(event);
                
                // 3. Update the read model using existing services
                updateReadModel(event);
                
                logger.info("Successfully processed event: {} for item: {}", 
                    event.getEventType(), event.getEntityId());
            } catch (Exception e) {
                logger.error("Error processing event: {} for item: {}", 
                    event.getEventType(), event.getEntityId(), e);
                throw e;
            }
        });
    }

    private void updateReadModel(DomainEvent event) {
        switch (event.getEventType()) {
            case "ITEM_CREATED" -> {
                var item = new Item((ItemCreatedEvent) event);
                itemService.createItem(item);
            }
        
            case "ITEM_POSITION_CHANGED" -> {
                var item = itemService.getItemById(event.getEntityId());
                var location = locationService.getLocationById(((ItemPositionChangedEvent) event).getLocationId());
                item.updatePosition(location);
                itemService.fullUpdateItem(item);
            }
        
            case "ITEM_SPEED_CHANGED" -> {
                var item = itemService.getItemById(event.getEntityId());
                item.updateSpeed((ItemSpeedChangedEvent) event);
                
                itemService.fullUpdateItem(item);
            }
        
            case "ITEM_DECTIVATED" -> {
                var item = itemService.getItemById(event.getEntityId());
                item.stop();
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("active", item.isActive());
                itemService.updateItem(new UpdateModel(item.getId(), updateData));
            }

            case "ITEM_ACTIVATED" -> {
                var item = itemService.getItemById(event.getEntityId());
                item.resume();
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("active", item.isActive());
                itemService.updateItem(new UpdateModel(item.getId(), updateData));
            }
        
            case "ITEM_PROPERTIES_UPDATED" -> {
                var item = itemService.getItemById(event.getEntityId());
                item.updateProperties((ItemPropertiesUpdatedEvent) event);
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("properties", item.getProperties());
                itemService.updateItem(new UpdateModel(item.getId(), updateData));
            }
        
            case "LOCATION_CREATED" ->  {
                var location = new Location((LocationCreatedEvent) event);
                locationService.createLocation(location);
            }
        
            case "LOCATION_PROPERTIES_UPDATED" -> {
                var location = locationService.getLocationById(event.getEntityId());
                location.updateProperties((LocationPropertiesUpdatedEvent) event);
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("properties", location.getProperties());
                
                locationService.updateLocation(new UpdateModel(location.getId(), updateData));
            }
        
            default -> logger.warn("Unknown event type: {}", event.getEventType());
        }
        
    }
} 