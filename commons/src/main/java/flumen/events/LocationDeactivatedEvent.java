package flumen.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class LocationDeactivatedEvent extends DomainEvent {
    @JsonCreator
    public LocationDeactivatedEvent(@JsonProperty("locationId") String locationId) {
        super(locationId, "LOCATION_ACTIVATED");
    }
} 