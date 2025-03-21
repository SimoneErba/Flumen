package com.livedatatrail.backend.controllers;

import com.livedatatrail.backend.models.GraphData;
import com.livedatatrail.backend.services.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/graph")
@Tag(name = "Graph", description = "APIs for retrieving graph data")
public class GraphController {

    private final GraphService graphService;

    @Autowired
    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping
    @Operation(summary = "Get the current state of the graph")
    public ResponseEntity<GraphData> getGraphData() {
        try {
            GraphData data = graphService.getGraphData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 