package se.cs.eventsourcing.domain.store.upgrade;

import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

import java.time.LocalDate;

@EventHasUpgrade(NewAnimalCreatedWithZooName.class)
public class NewAnimalCreatedWithBirthdate extends AbstractDomainEvent {

    private final String name;
    private final LocalDate birthDate;

    public NewAnimalCreatedWithBirthdate(String name, LocalDate birthDate) {
        super(1);
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