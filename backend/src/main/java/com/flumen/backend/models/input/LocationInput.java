package com.flumen.backend.models.input;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationInput {
	private String id;
	private String name;
	private Double latitude;
	private Double longitude;
	private Double length;
    private Double speed;
    private String type;
    private Boolean active;
	private Map<String, Object> properties;
}
