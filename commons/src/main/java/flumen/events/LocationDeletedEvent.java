package flumen.events;

import lombok.Getter;

@Getter
public class LocationDeletedEvent extends DomainEvent {
    public LocationDeletedEvent(String locationId) {
        super(locationId, "LOCATION_DELETED");
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
} 