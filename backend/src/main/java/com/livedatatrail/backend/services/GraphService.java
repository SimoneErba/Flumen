package com.livedatatrail.backend.services;

import com.livedatatrail.backend.models.GraphData;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.OEdge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GraphService {
    private final OrientDBService orientDBService;
    private static final Logger logger = LoggerFactory.getLogger(GraphService.class);

    @Autowired
    public GraphService(OrientDBService orientDBService) {
        this.orientDBService = orientDBService;
    }

    private Map<String, Object> extractSafeProperties(OVertex vertex) {
        Map<String, Object> properties = new HashMap<>();
        vertex.getPropertyNames().stream()
            .filter(name -> !name.startsWith("out_") && !name.startsWith("in_") && !name.equals("@rid"))
            .forEach(name -> {
                Object value = vertex.getProperty(name);
                // Only include primitive types and strings
                if (value == null || value instanceof Number || value instanceof String || value instanceof Boolean) {
                    properties.put(name, value);
                }
            });
        return properties;
    }

    private Map<String, Object> extractSafeProperties(OEdge edge) {
        Map<String, Object> properties = new HashMap<>();
        edge.getPropertyNames().stream()
            .filter(name -> !name.startsWith("out_") && !name.startsWith("in_") && !name.equals("@rid"))
            .forEach(name -> {
                Object value = edge.getProperty(name);
                // Only include primitive types and strings
                if (value == null || value instanceof Number || value instanceof String || value instanceof Boolean) {
                    properties.put(name, value);
                }
            });
        return properties;
    }

    public GraphData getGraphData() {
        List<GraphData.LocationData> locations = new ArrayList<>();
        List<GraphData.Edge> edges = new ArrayList<>();

        try (ODatabaseSession session = orientDBService.getSession()) {
            // Fetch all items and their associated location in one query
            Map<String, List<GraphData.ItemData>> locationToItemsMap = new HashMap<>();

            // Using a query to fetch items along with their locations (grouped by locationId)
            /*session.query("SELECT customId, locationId FROM Item")
                .stream()
                .forEach(result -> {
                    OVertex vertex = result.getVertex().get();
                    String itemId = vertex.getProperty("customId");
                    String locationId = vertex.getProperty("locationId"); // Assuming there's a locationId field
                    if (itemId != null && locationId != null) {
                        // Grouping items by locationId
                        GraphData.ItemData itemData = new GraphData.ItemData(itemId, extractSafeProperties(vertex));
                        locationToItemsMap.computeIfAbsent(locationId, k -> new ArrayList<>()).add(itemData);
                    }
                });*/

            // Fetch all locations, now associating items directly with each location
            /*session.query("SELECT customId FROM Location")
                .stream()
                .forEach(result -> {
                    OVertex vertex = result.getVertex().get();
                    String locationId = vertex.getProperty("customId");
                    if (locationId != null) {
                        // Use the map to fetch the items for the current location
                        GraphData.LocationData locationData = new GraphData.LocationData(
                            locationId,
                            extractSafeProperties(vertex),
                            locationToItemsMap.getOrDefault(locationId, new ArrayList<>()) // Assign the grouped items
                        );
                        locations.add(locationData);
                    }
                });*/

            // Fetch all position edges (connections between locations)
            session.query("SELECT *, in('HasPosition') FROM Location")
                .stream()
                .forEach(result -> {
                    OEdge edge = result.getEdge().get();
                    logger.info("Edge: {}", edge);
                    /*
                    String fromLocationId = edge.getFrom().getProperty("customId");
                    String toLocationId = edge.getTo().getProperty("customId");
                    if (fromLocationId != null && toLocationId != null) {
                        edges.add(new GraphData.Edge(
                            fromLocationId + "_to_" + toLocationId,  // Generate a deterministic edge ID
                            fromLocationId,
                            toLocationId,
                            "position",
                            extractSafeProperties(edge)
                        ));
                    }*/
                });

            // Fetch all connection edges
            session.query("SELECT FROM ConnectedTo")
                .stream()
                .forEach(result -> {
                    OEdge edge = result.getEdge().get();
                    String fromLocationId = edge.getFrom().getProperty("customId");
                    String toLocationId = edge.getTo().getProperty("customId");
                    if (fromLocationId != null && toLocationId != null) {
                        edges.add(new GraphData.Edge(
                            fromLocationId + "_to_" + toLocationId,  // Generate a deterministic edge ID
                            fromLocationId,
                            toLocationId,
                            "connected_to",
                            extractSafeProperties(edge)
                        ));
                    }
                });
        }

        return new GraphData(locations, edges);
    }
}
