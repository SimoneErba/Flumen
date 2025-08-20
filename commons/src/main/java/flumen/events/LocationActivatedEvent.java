package flumen.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class LocationActivatedEvent extends DomainEvent {

    @JsonCreator
    public LocationActivatedEvent(@JsonProperty("locationId") String locationId) {
        super(locationId, "LOCATION_ACTIVATED");
    }
} 