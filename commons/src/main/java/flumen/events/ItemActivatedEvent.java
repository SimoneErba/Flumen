package flumen.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class ItemActivatedEvent extends DomainEvent {
    @JsonCreator
    public ItemActivatedEvent(@JsonProperty("itemId") String itemId) {
        super(itemId, "ITEM_ACTIVATED");
    }
} 