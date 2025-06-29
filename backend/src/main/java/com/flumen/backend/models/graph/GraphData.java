package com.flumen.backend.models.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.flumen.backend.models.response.ConnectionResponse;
import com.flumen.backend.models.response.LocationResponse;

import lombok.Data;

@Data
public class GraphData {
    private List<LocationResponse> locations;
    private List<ConnectionResponse> connections;

    public GraphData() {
        this.locations = new ArrayList<>();
        this.connections = new ArrayList<>();
    }

    public void addLocation(LocationResponse location) {
        this.locations.add(location);
    }

    public void addConnections(List<ConnectionResponse> newConnections) {
        this.connections.addAll(newConnections);
    }
}