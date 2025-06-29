package com.flumen.backend.services;

import com.clickhouse.client.ClickHouseRequest;
import com.clickhouse.data.ClickHouseFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flumen.backend.models.graph.GraphData;

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
            GraphData graphState = graphService.getGraphData();
            String graphJson = objectMapper.writeValueAsString(graphState);
            
            // Create snapshot metadata
            String snapshotId = UUID.randomUUID().toString();
            Instant timestamp = Instant.now();
            
            // Save to ClickHouse
            String sql = "INSERT INTO snapshots (snapshot_id, timestamp, graph_data) VALUES";
            
            // TODO: INSERT SNAPSHOT IN CH
            
            logger.info("Graph snapshot completed successfully. Snapshot ID: {}", snapshotId);
            
        } catch (Exception e) {
            logger.error("Failed to take graph snapshot", e);
        }
    }
} 