package com.livedatatrail.backend.models.entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private String id;
    private String name;
    private Double speed;
    private Boolean active; 
    private Map<String, Object> properties;
} 