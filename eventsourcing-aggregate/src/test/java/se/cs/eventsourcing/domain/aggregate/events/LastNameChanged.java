package se.cs.eventsourcing.domain.aggregate.events;

import com.google.common.base.Preconditions;
import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

public class LastNameChanged extends AbstractDomainEvent {

    private String newValue;

    public LastNameChanged(String newValue) {
        super(1);
        this.newValue = Preconditions.checkNotNull(newValue);
    }

    public String getLastName() {
        return newValue;
    }}
