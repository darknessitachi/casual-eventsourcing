package se.cs.eventsourcing.infrastructure.store.jdbc.sample;

import se.cs.eventsourcing.domain.store.event.DomainEvent;

public class ChangeLastName implements DomainEvent {

    private String name;

    private ChangeLastName() {}

    public ChangeLastName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ChangeLastName{" +
                "name='" + name + '\'' +
                '}';
    }
}