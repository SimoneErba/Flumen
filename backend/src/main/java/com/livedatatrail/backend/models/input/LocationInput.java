package com.livedatatrail.backend.models.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationInput {
	private String name;
	private double latitude;
	private double longitude;
}
