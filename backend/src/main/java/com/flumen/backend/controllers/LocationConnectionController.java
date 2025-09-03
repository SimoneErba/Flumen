package com.flumen.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.flumen.backend.models.input.ConnectionInput;
import com.flumen.backend.services.ConnectedToService;

@RestController
@RequestMapping("/api/connections")
public class LocationConnectionController {

    private final ConnectedToService connectedToService;

    @Autowired
    public LocationConnectionController(ConnectedToService connectedToService) {
        this.connectedToService = connectedToService;
    }

    /**
     * Creates a connection between two locations.
     * @param connectionInput The connection details (from location -> to location).
     * @return A response indicating success or failure.
     */
    @PostMapping
    public void createConnection(@RequestBody ConnectionInput connectionInput) {
        connectedToService.createConnection(connectionInput.getLocation1Id(), connectionInput.getLocation2Id());
    }

    /**
     * Moves a connection from one location to another.
     * @param connectionInput The connection details (source location -> new target location).
     * @return A response indicating success or failure.
     */
    @PutMapping
    public void moveConnection(@RequestBody ConnectionInput connectionInput) {
        connectedToService.moveConnection(connectionInput.getLocation1Id(), connectionInput.getLocation2Id());
    }

    /**
     * Deletes all connections from a specific location.
     * @param locationId The location ID whose connections need to be deleted.
     * @return A response indicating success or failure.
     */
    @DeleteMapping("/{locationId}")
    public void deleteConnections(@PathVariable String locationId) {
        connectedToService.deleteConnections(locationId);
    }

    @DeleteMapping
    public void deleteConnection(String sourceId, String targetId) {
        connectedToService.deleteConnection(sourceId, targetId);
    }
}
