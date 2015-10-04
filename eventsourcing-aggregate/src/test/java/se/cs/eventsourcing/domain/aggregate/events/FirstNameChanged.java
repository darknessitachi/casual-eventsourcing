package se.cs.eventsourcing.domain.aggregate.events;

import com.google.common.base.Preconditions;
import se.cs.eventsourcing.domain.store.event.DomainEvent;

public class FirstNameChanged implements DomainEvent {

    private String newValue;

    public FirstNameChanged(String newValue) {
        this.newValue = Preconditions.checkNotNull(newValue);
    }

    public String getFirstName() {
        return newValue;
    }
}
