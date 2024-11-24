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
public class PositionService {

    private static final Logger logger = LoggerFactory.getLogger(PositionService.class);

    private final OrientDBService orientDBService;

    @Autowired
    public PositionService(OrientDBService orientDBService) {
        this.orientDBService = orientDBService;
    }

    public void createConnection(String itemId, String locationId) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            OElement item = OrientDBUtils.loadAndValidateVertexByName(db, itemId);
            OElement location = OrientDBUtils.loadAndValidateVertexByName(db, locationId);

            item.asVertex().get().addEdge(location.asVertex().get(), "HasPosition");
        }
    }

    public void moveConnection(String itemId, String newLocationId) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            OElement item = OrientDBUtils.loadAndValidateVertexByName(db, itemId);
            OElement newLocation = OrientDBUtils.loadAndValidateVertexByName(db, newLocationId);

            var edges = item.asVertex().get().getEdges(ODirection.OUT, "HasPosition");
            for (var edge : edges) {
                edge.delete();
            }
            item.asVertex().get().addEdge(newLocation.asVertex().get(), "HasPosition");
        }
    }

    public void deleteConnections(String itemId) {
        try (ODatabaseSession db = orientDBService.getSession()) {
            OElement item = OrientDBUtils.loadAndValidateVertexByName(db, itemId);

            var edges = item.asVertex().get().getEdges(ODirection.OUT, "HasPosition");
            for (var edge : edges) {
                edge.delete();
            }
        }
    }
}
