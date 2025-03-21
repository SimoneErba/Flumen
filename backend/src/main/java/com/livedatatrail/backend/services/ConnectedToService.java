package com.livedatatrail.backend.services;

import com.livedatatrail.backend.utils.OrientDBUtils;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OElement;
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
}
