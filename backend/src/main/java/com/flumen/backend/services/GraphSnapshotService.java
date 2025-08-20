package com.flumen.backend.services;

import com.flumen.backend.models.graph.GraphData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class GraphSnapshotService {
    private static final Logger logger = LoggerFactory.getLogger(GraphSnapshotService.class);

    private final GraphService graphService;
    private final ClickHouseService clickHouseService;

    // Use constructor injection for dependencies
    public GraphSnapshotService(GraphService graphService, ClickHouseService clickHouseService) {
        this.graphService = graphService;
        this.clickHouseService = clickHouseService;
    }

    /**
     * Periodically takes a snapshot of the current graph state from OrientDB
     * and saves it as a historical record in ClickHouse.
     */
    @Scheduled(fixedRate = 3600000) // Runs every hour
    public void takeSnapshot() {
        try {
            logger.info("Starting graph snapshot process...");

            // Step 1: Get the current state of the graph from the graph service.
            GraphData graphState = graphService.getGraphData();

            // Step 2: Create snapshot metadata.
            String snapshotId = UUID.randomUUID().toString();
            Instant timestamp = Instant.now();

            // Step 3: Delegate the saving operation to the ClickHouseService.
            // This cleanly separates the responsibility of data retrieval from data persistence.
            clickHouseService.saveSnapshot(snapshotId, timestamp, graphState);

            logger.info("Graph snapshot completed successfully. Snapshot ID: {}", snapshotId);

        } catch (Exception e) {
            // The service method already logs the detailed error, so we log a higher-level message here.
            logger.error("The scheduled graph snapshot task failed.", e);
        }
    }
}