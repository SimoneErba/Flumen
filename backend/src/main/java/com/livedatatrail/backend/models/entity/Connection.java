package com.livedatatrail.backend.models.entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Connection {
    private String sourceId;
    private String targetId;
    private String type;  // Direction: "in" or "out"
    private Map<String, Object> properties;
} 