package se.cs.eventsourcing.domain.store.upgrade;

import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

@EventHasUpgrade(NewAnimalCreatedWithBirthdate.class)
public class NewAnimalCreated extends AbstractDomainEvent {

    private final String name;

    NewAnimalCreated(String name) {
        super(1);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}