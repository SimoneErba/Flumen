package com.flumen.backend.models.input;

import java.util.Map;

import flumen.events.ItemCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInput {
	private String id;
	private String name;
	private Double speed;
    private Boolean active; 
	private Map<String, Object> properties;


    public ItemInput (ItemCreatedEvent event) {
        this.id = event.getEntityId();
        this.name = event.getName();
        this.speed = event.getSpeed();
        this.active = event.isActive();
        this.properties = event.getProperties();
    }
}
