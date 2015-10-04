package se.cs.eventsourcing.domain.store.event;

import java.time.LocalDate;

public class NewAnimalCreatedWithZooName implements DomainEvent {

    private final String name;
    private final LocalDate birthDate;
    private final String zooName;

    public NewAnimalCreatedWithZooName(String name, LocalDate birthDate, String zooName) {
        this.name = name;
        this.birthDate = birthDate;
        this.zooName = zooName;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getZooName() {
        return zooName;
    }
}