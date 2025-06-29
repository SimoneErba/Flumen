package flumen.events;

import lombok.Getter;

@Getter
public class ItemDeactivatedEvent extends DomainEvent {
    public ItemDeactivatedEvent(String itemId) {
        super(itemId, "ITEM_DEACTIVATED");
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }
} 