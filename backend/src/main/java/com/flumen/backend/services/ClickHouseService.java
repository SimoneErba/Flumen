package com.flumen.backend.services;

import com.clickhouse.client.api.Client;
import com.clickhouse.data.ClickHouseFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import flumen.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
public class ClickHouseService {
    private static final Logger logger = LoggerFactory.getLogger(ClickHouseService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Client client;

    @Value("${clickhouse.url}")
    private String clickhouseUrl;

    @Value("${clickhouse.username}")
    private String username;

    @Value("${clickhouse.password}")
    private String password;

    @PostConstruct
    public void init() {
        try {
            client = new Client.Builder()
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
            String json = objectMapper.writeValueAsString(event);
            String wrappedRow = String.format("""
                    {
                      "event_id": "%s",
                      "entity_id": "%s",
                      "event_type": "%s",
                      "timestamp": "%s",
                      "event_data": %s
                    }
                    """,
                    event.getEventId(),
                    event.getEntityId(),
                    event.getEventType(),
                    event.getTimestamp(),
                    json
            );

            try (var inputStream = new ByteArrayInputStream(wrappedRow.getBytes(StandardCharsets.UTF_8))) {
                client.insert("events", inputStream, ClickHouseFormat.JSONEachRow);
            }

            logger.debug("Event saved: {}", event.getEventId());

        } catch (Exception e) {
            logger.error("Error saving event to ClickHouse", e);
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
}
