package com.livedatatrail.backend.controllers;

import com.livedatatrail.backend.models.input.CreateConnection;
import com.livedatatrail.backend.services.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService positionService;

    @Autowired
    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping()
    public ResponseEntity<String> createConnection(@RequestBody CreateConnection model) {
        try {
            positionService.createConnection(model.getItemId(), model.getLocationId());
            return ResponseEntity.ok("Connection created successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(String.format("Error while createConnection: %s", e.getMessage()));
        }
    }

    @PutMapping()
    public ResponseEntity<String> moveConnection(@RequestBody CreateConnection model) {
        try {
            positionService.moveConnection(model.getItemId(), model.getLocationId());
            return ResponseEntity.ok("Connection moved successfully.");

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(String.format("Error while moveConnection: %s", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConnections(@RequestParam String itemId) {
        try {
            positionService.deleteConnections(itemId);
            return ResponseEntity.ok("Connections deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(String.format("Error while deleteConnections: %s", e.getMessage()));
        }
    }
}
