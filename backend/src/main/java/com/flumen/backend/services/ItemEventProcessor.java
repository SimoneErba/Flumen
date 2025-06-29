package com.flumen.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flumen.backend.domain.Item;
import com.flumen.backend.domain.Location;
import flumen.events.DomainEvent;
import flumen.events.ItemCreatedEvent;
import flumen.events.ItemPositionChangedEvent;
import flumen.events.ItemPropertiesUpdatedEvent;
import flumen.events.ItemSpeedChangedEvent;
import flumen.events.LocationCreatedEvent;
import flumen.events.LocationPropertiesUpdatedEvent;
import com.flumen.backend.models.UpdateModel;
import com.flumen.backend.models.input.ItemInput;
import com.flumen.backend.models.input.LocationInput;

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
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    private void processEvent(DomainEvent event) {
        switch (event.getEventType()) {
            case "ITEM_CREATED" -> {
                var item = new Item((ItemCreatedEvent) event);
                ItemInput itemInput = objectMapper.convertValue(item, ItemInput.class);
                itemService.createItem(itemInput);
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
                LocationInput locationInput = objectMapper.convertValue(location, LocationInput.class);
                locationService.createLocation(locationInput);
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