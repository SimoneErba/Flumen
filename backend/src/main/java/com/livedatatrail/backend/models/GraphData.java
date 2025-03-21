package com.livedatatrail.backend.models;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphData {
    private List<LocationData> locations;
    private List<Edge> edges;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private String locationId;
        private Map<String, Object> locationProperties; // Properties specific to the location
        private List<ItemData> items; // List of items at this location
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemData {
        private String itemId;
        private Map<String, Object> itemProperties; // Properties specific to the item
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        private String id;
        private String source;
        private String target;
        private String type; // "connection"
        private Map<String, Object> properties;
    }
}
