package com.livedatatrail.backend.entities;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String id;
    private String name;
    private Boolean active;
    private Map<String, Object> properties;
} 