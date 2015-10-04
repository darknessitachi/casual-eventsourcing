package se.cs.eventsourcing.domain.aggregate.events;

import com.google.common.base.Preconditions;
import se.cs.eventsourcing.domain.store.DomainEvent;

public class LastNameChanged implements DomainEvent {

    private String newValue;

    public LastNameChanged(String newValue) {
        this.newValue = Preconditions.checkNotNull(newValue);
    }

    public String getLastName() {
        return newValue;
    }}
