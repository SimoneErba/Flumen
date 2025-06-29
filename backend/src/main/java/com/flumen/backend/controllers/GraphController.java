package com.flumen.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flumen.backend.models.graph.GraphData;
import com.flumen.backend.services.GraphService;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/graph")
@Tag(name = "Graph", description = "APIs for retrieving graph data")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping
    @Operation(summary = "Get the current state of the graph")
    public ResponseEntity<GraphData> getGraphData() {
        return ResponseEntity.ok(graphService.getGraphData());
    }
} 