package com.livedatatrail.backend.models.input;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInput {
	private String id;
	private String name;
	private Map<String, Object> properties;
}
