package com.flumen.backend.services;

import com.clickhouse.client.api.Client;
import com.clickhouse.data.ClickHouseFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flumen.backend.models.graph.GraphData;

import flumen.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ClickHouseService {
    private static final Logger logger = LoggerFactory.getLogger(ClickHouseService.class);

    private final Client client;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter CLICKHOUSE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);
    public ClickHouseService(
            @Value("${clickhouse.url}") String clickhouseUrl,
            @Value("${clickhouse.username}") String username,
            @Value("${clickhouse.password}") String password,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        try {
            this.client = new Client.Builder()
                    .addEndpoint(clickhouseUrl)
                    .setUsername(username)
                    .setPassword(password)
                    .build();
            logger.info("ClickHouse Client V2 initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize ClickHouse client", e);
            throw new RuntimeException("ClickHouse init error", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (client != null) {
            client.close();
        }
    }

    public void saveEvent(DomainEvent event) {
        try {
            Map<String, Object> clickHouseRow = new HashMap<>();
            String formattedTimestamp = CLICKHOUSE_FORMATTER.format(event.getTimestamp());
            clickHouseRow.put("timestamp", formattedTimestamp);
            clickHouseRow.put("event_type", event.getEventType());
            clickHouseRow.put("entity_id", event.getEntityId());
            clickHouseRow.put("event_id", event.getEventId());
            clickHouseRow.put("data", event);

            String finalJson = objectMapper.writeValueAsString(clickHouseRow);

            try (var inputStream = new ByteArrayInputStream(finalJson.getBytes(StandardCharsets.UTF_8))) {
                client.insert("Events", inputStream, ClickHouseFormat.JSONEachRow);
            }

            logger.debug("Event saved: {}", event.getEventId());

        } catch (Exception e) {
            logger.error("Error saving event {} to ClickHouse", event.getEventId(), e);
            throw new RuntimeException("Save failed", e);
        }
    }

    public void saveEventAsync(DomainEvent event) {
        CompletableFuture.runAsync(() -> saveEvent(event))
                .exceptionally(ex -> {
                    logger.error("Async error saving event {}", event.getEventId(), ex);
                    return null;
                });
    }

    /**
     * Saves a graph snapshot to the 'snapshots' table in ClickHouse.
     * @param snapshotId The unique ID for the snapshot.
     * @param timestamp The time the snapshot was taken.
     * @param graphData The graph data object to be serialized and stored.
     */
    public void saveSnapshot(String snapshotId, Instant timestamp, GraphData graphData) {
        try {
            // Create a Map that directly matches the 'snapshots' table columns.
            Map<String, Object> clickHouseRow = new HashMap<>();

            clickHouseRow.put("snapshot_id", snapshotId);
            clickHouseRow.put("timestamp", CLICKHOUSE_FORMATTER.format(timestamp));
            
            // Let Jackson serialize the rich GraphData object into a nested JSON object for the 'graph_data' column.
            clickHouseRow.put("graph_data", graphData);

            // Serialize the entire row map into a single JSON string for insertion.
            String finalJson = objectMapper.writeValueAsString(clickHouseRow);

            // Insert into the 'snapshots' table using the JSONEachRow format.
            try (var inputStream = new ByteArrayInputStream(finalJson.getBytes(StandardCharsets.UTF_8))) {
                client.insert("snapshots", inputStream, ClickHouseFormat.JSONEachRow);
            }

            logger.info("Successfully saved graph snapshot with ID: {}", snapshotId);

        } catch (Exception e) {
            logger.error("Error saving snapshot {} to ClickHouse", snapshotId, e);
            throw new RuntimeException("Snapshot save failed", e);
        }
    }
}