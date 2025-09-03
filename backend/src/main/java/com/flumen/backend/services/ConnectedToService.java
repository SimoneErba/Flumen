package com.flumen.backend.services;

import com.flumen.backend.utils.OrientDBUtils;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.id.ORID;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConnectedToService {

    private static final Logger logger = LoggerFactory.getLogger(ConnectedToService.class);

    private final OrientDBService orientDBService;

    @Autowired
    public ConnectedToService(OrientDBService orientDBService) {
        this.orientDBService = orientDBService;
    }

    /**
     * Creates a directed connection between two locations (location 1 -> location 2).
     * @param location1Id The ID of the first location (source).
     * @param location2Id The ID of the second location (target).
     */
    public void createConnection(String location1Id, String location2Id) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            OElement location1 = OrientDBUtils.loadAndValidateVertexByCustomId(db, location1Id);
            OElement location2 = OrientDBUtils.loadAndValidateVertexByCustomId(db, location2Id);

            location1.asVertex().get().addEdge(location2.asVertex().get(), "ConnectedTo");
            location1.save();
        }
    }

    /**
     * Moves a connection from one location to another, meaning the connection direction is changed.
     * @param location1Id The ID of the source location.
     * @param location2Id The ID of the new target location.
     */
    public void moveConnection(String location1Id, String location2Id) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            OElement location1 = OrientDBUtils.loadAndValidateVertexByCustomId(db, location1Id);
            OElement location2 = OrientDBUtils.loadAndValidateVertexByCustomId(db, location2Id);

            // Delete the existing "ConnectedTo" edge
            var edges = location1.asVertex().get().getEdges(ODirection.OUT, "ConnectedTo");
            for (var edge : edges) {
                edge.delete();
            }

            // Create a new edge from location1 to location2
            location1.asVertex().get().addEdge(location2.asVertex().get(), "ConnectedTo");
            location1.save();
        }
    }

    /**
     * Deletes all connections from a specific location.
     * @param locationId The ID of the location whose connections should be deleted.
     */
    public void deleteConnections(String locationId) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            OElement location = OrientDBUtils.loadAndValidateVertexByCustomId(db, locationId);

            // Delete all outgoing "ConnectedTo" edges from the location
            var edges = location.asVertex().get().getEdges(ODirection.OUT, "ConnectedTo");
            for (var edge : edges) {
                edge.delete();
            }
        }
    }

    /**
     * Deletes all direct edges from a source vertex to a target vertex.
     *
     * @param sourceId The unique ID property of the source vertex.
     * @param targetId The unique ID property of the target vertex.
     */
    public void deleteConnection(String sourceId, String targetId) {
        // A helpful check to prevent errors with bad input
        if (sourceId == null || targetId == null || sourceId.isEmpty() || targetId.isEmpty()) {
            throw new IllegalArgumentException("Source and Target IDs cannot be null or empty.");
        }

        // This query finds the edge(s) between the two vertices and deletes them.
        // NOTE: This assumes you have a property named 'id' on your Location/Vertex class.
        // If your unique identifier is different (e.g., 'name' or 'uuid'), change the query accordingly.
        String query = "DELETE EDGE E FROM (SELECT FROM V WHERE customId = :sourceId) TO (SELECT FROM V WHERE customId = :targetId)";

        try (ODatabaseSession db = orientDBService.getSession()) {
            Map<String, Object> params = new HashMap<>();
            params.put("sourceId", sourceId);
            params.put("targetId", targetId);

            // Execute the command. The result tells you how many edges were deleted.
            try (OResultSet rs = db.command(query, params)) {
                long deletedCount = rs.stream().map(r -> r.<Long>getProperty("count")).findFirst().orElse(0L);
                if (deletedCount == 0) {
                    System.out.println("No connection found from " + sourceId + " to " + targetId + " to delete.");
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Error while deleting connection from %s to %s: %s",
                                                sourceId, targetId, e.getMessage());
            throw new RuntimeException(errorMessage, e);
        }
    }
}
