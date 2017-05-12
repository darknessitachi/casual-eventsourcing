package se.cs.eventsourcing.domain.store.event;

import se.cs.eventsourcing.domain.event.DomainEvent;

@EventHasUpgrade(NewAnimalCreatedWithBirthdate.class)
public class NewAnimalCreated implements DomainEvent {

    private final String name;

    public NewAnimalCreated(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}