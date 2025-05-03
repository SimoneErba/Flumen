package com.livedatatrail.backend.services;

import com.clickhouse.client.*;
import com.clickhouse.data.ClickHouseFormat;
import com.clickhouse.data.ClickHouseRecord;
import com.clickhouse.data.ClickHouseValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livedatatrail.backend.domain.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;

@Service
public class ClickHouseService {
    private static final Logger logger = LoggerFactory.getLogger(ClickHouseService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ClickHouseClient client;
    private ClickHouseNode server;

    @Value("${clickhouse.url}")
    private String clickhouseUrl;

    @Value("${clickhouse.username}")
    private String username;

    @Value("${clickhouse.password}")
    private String password;

    public ClickHouseClient getClient() {
        return client;
    }

    public ClickHouseNode getServer() {
        return server;
    }

    @PostConstruct
    public void init() {
        try {
            client = ClickHouseClient.newInstance(ClickHouseProtocol.HTTP);
            server = ClickHouseNode.builder()
                    .host(clickhouseUrl.replace("jdbc:clickhouse://", "").split("/")[0].split(":")[0])
                    .port(ClickHouseProtocol.HTTP, 8123)
                    .credentials(ClickHouseCredentials.fromUserAndPassword(username, password))
                    .database("default")
                    .build();
            
            logger.info("ClickHouse client initialized");
        } catch (Exception e) {
            logger.error("Failed to initialize ClickHouse client", e);
            throw new RuntimeException("Failed to initialize ClickHouse client", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (client != null) {
            client.close();
        }
    }

    public void saveEvent(DomainEvent event) {
        String sql = "INSERT INTO events (event_id, entity_id, event_type, timestamp, event_data) VALUES";
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            CompletableFuture<Integer> future = client.execute(server, ClickHouseRequest.builder()
                    .query(sql)
                    .format(ClickHouseFormat.JSONEachRow)
                    .data(String.format("""
                            {"event_id": "%s", "entity_id": "%s", "event_type": "%s", "timestamp": "%s", "event_data": %s}
                            """,
                            event.getEventId(),
                            event.getEntityId(),
                            event.getEventType(),
                            event.getTimestamp().toString(),
                            eventJson))
                    .build());

            future.get(); // Wait for completion
            logger.debug("Event saved to ClickHouse: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Error saving event to ClickHouse: {}", event.getEventId(), e);
            throw new RuntimeException("Failed to save event to ClickHouse", e);
        }
    }

    public void saveEventAsync(DomainEvent event) {
        CompletableFuture.runAsync(() -> saveEvent(event))
            .exceptionally(throwable -> {
                logger.error("Async error saving event to ClickHouse: {}", event.getEventId(), throwable);
                return null;
            });
    }

    public CompletableFuture<ClickHouseResponse> queryEvents(String sql) {
        return client.execute(server, ClickHouseRequest.builder()
                .query(sql)
                .format(ClickHouseFormat.JSONEachRow)
                .build());
    }

    // Example method to get events for an item
    public CompletableFuture<Iterable<ClickHouseRecord>> getEventsForItem(String itemId) {
        String sql = String.format(
            "SELECT * FROM events WHERE entity_id = '%s' ORDER BY timestamp",
            itemId
        );
        
        return queryEvents(sql)
            .thenApply(response -> {
                try {
                    return response.records();
                } catch (Exception e) {
                    logger.error("Error reading events from ClickHouse", e);
                    throw new RuntimeException(e);
                }
            });
    }

    // Example method to get event counts by type
    public CompletableFuture<Iterable<ClickHouseRecord>> getEventCountsByType() {
        String sql = "SELECT event_type, count(*) as count FROM events GROUP BY event_type";
        
        return queryEvents(sql)
            .thenApply(response -> {
                try {
                    return response.records();
                } catch (Exception e) {
                    logger.error("Error reading event counts from ClickHouse", e);
                    throw new RuntimeException(e);
                }
            });
    }
} 