package se.cs.eventsourcing.domain.aggregate.events;

import com.google.common.base.Preconditions;
import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

public class FirstNameChanged extends AbstractDomainEvent {

    private String newValue;

    public FirstNameChanged(String newValue) {
        super(1);
        this.newValue = Preconditions.checkNotNull(newValue);
    }

    public String getFirstName() {
        return newValue;
    }
}
