package com.livedatatrail.backend.services;

import com.clickhouse.client.ClickHouseRequest;
import com.clickhouse.data.ClickHouseFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class GraphSnapshotService {
    private static final Logger logger = LoggerFactory.getLogger(GraphSnapshotService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final GraphService graphService;
    private final ClickHouseService clickHouseService;

    public GraphSnapshotService(GraphService graphService, ClickHouseService clickHouseService) {
        this.graphService = graphService;
        this.clickHouseService = clickHouseService;
    }

    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
    public void takeSnapshot() {
        try {
            logger.info("Starting graph snapshot...");
            
            // Get the current state of the graph
            Map<String, Object> graphState = graphService.getGraphState();
            String graphJson = objectMapper.writeValueAsString(graphState);
            
            // Create snapshot metadata
            String snapshotId = UUID.randomUUID().toString();
            Instant timestamp = Instant.now();
            
            // Save to ClickHouse
            String sql = "INSERT INTO snapshots (snapshot_id, timestamp, graph_data) VALUES";
            
            clickHouseService.getClient().execute(
                clickHouseService.getServer(),
                ClickHouseRequest.builder()
                    .query(sql)
                    .format(ClickHouseFormat.JSONEachRow)
                    .data(String.format("""
                            {"snapshot_id": "%s", "timestamp": "%s", "graph_data": %s}
                            """,
                            snapshotId,
                            timestamp.toString(),
                            graphJson))
                    .build()
            ).get(); // Wait for completion
            
            logger.info("Graph snapshot completed successfully. Snapshot ID: {}", snapshotId);
            
        } catch (Exception e) {
            logger.error("Failed to take graph snapshot", e);
        }
    }

    // Method to get a specific snapshot
    public Map<String, Object> getSnapshot(String snapshotId) {
        try {
            String sql = String.format(
                "SELECT graph_data FROM snapshots WHERE snapshot_id = '%s'",
                snapshotId
            );
            
            return clickHouseService.queryEvents(sql)
                .thenApply(response -> {
                    try {
                        var record = response.records().iterator().next();
                        String graphJson = record.getValue("graph_data").asString();
                        return objectMapper.readValue(graphJson, Map.class);
                    } catch (Exception e) {
                        logger.error("Error reading snapshot data", e);
                        throw new RuntimeException(e);
                    }
                }).get();
        } catch (Exception e) {
            logger.error("Failed to get snapshot {}", snapshotId, e);
            throw new RuntimeException("Failed to get snapshot", e);
        }
    }

    // Method to get the latest snapshot
    public Map<String, Object> getLatestSnapshot() {
        try {
            String sql = "SELECT graph_data FROM snapshots ORDER BY timestamp DESC LIMIT 1";
            
            return clickHouseService.queryEvents(sql)
                .thenApply(response -> {
                    try {
                        var record = response.records().iterator().next();
                        String graphJson = record.getValue("graph_data").asString();
                        return objectMapper.readValue(graphJson, Map.class);
                    } catch (Exception e) {
                        logger.error("Error reading latest snapshot data", e);
                        throw new RuntimeException(e);
                    }
                }).get();
        } catch (Exception e) {
            logger.error("Failed to get latest snapshot", e);
            throw new RuntimeException("Failed to get latest snapshot", e);
        }
    }
} 