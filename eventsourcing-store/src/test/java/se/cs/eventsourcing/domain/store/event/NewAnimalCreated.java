package se.cs.eventsourcing.domain.store.event;

@EventHasUpgrade(NewAnimalCreatedWithBirthdate.class)
public class NewAnimalCreated implements DomainEvent {

    private final String name;

    NewAnimalCreated(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}