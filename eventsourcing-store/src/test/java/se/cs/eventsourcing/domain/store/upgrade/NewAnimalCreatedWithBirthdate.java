package se.cs.eventsourcing.domain.store.upgrade;

import se.cs.eventsourcing.domain.store.DomainEvent;

import java.time.LocalDate;

@EventHasUpgrade(NewAnimalCreatedWithZooName.class)
public class NewAnimalCreatedWithBirthdate implements DomainEvent {

    private final String name;
    private final LocalDate birthDate;

    public NewAnimalCreatedWithBirthdate(String name, LocalDate birthDate) {
        this.name = name;
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
}