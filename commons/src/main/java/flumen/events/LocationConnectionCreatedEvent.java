package flumen.events;

public class LocationConnectionCreatedEvent extends DomainEvent {

    private String location1Id;
    private String location2Id;

    // A no-argument constructor is often needed for deserialization frameworks like Jackson
    private LocationConnectionCreatedEvent() {
        super();
    }

    public LocationConnectionCreatedEvent(String location1Id, String location2Id) {
        super(); // Initializes the timestamp in the parent DomainEvent
        this.location1Id = location1Id;
        this.location2Id = location2Id;
    }

    public String getLocation1Id() {
        return location1Id;
    }

    public String getLocation2Id() {
        return location2Id;
    }
}