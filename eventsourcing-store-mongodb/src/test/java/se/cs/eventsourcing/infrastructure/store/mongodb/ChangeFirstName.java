package se.cs.eventsourcing.infrastructure.store.mongodb;

import se.cs.eventsourcing.domain.event.DomainEvent;

public class ChangeFirstName implements DomainEvent {

    private String name;

    private ChangeFirstName() {}

    public ChangeFirstName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ChangeFirstName{" +
                "name='" + name + '\'' +
                '}';
    }
}
