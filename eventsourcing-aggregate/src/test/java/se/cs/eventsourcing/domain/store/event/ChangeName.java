package se.cs.eventsourcing.domain.store.event;

import se.cs.eventsourcing.domain.event.DomainEvent;

public class ChangeName implements DomainEvent {

    private final String name;

    public ChangeName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}