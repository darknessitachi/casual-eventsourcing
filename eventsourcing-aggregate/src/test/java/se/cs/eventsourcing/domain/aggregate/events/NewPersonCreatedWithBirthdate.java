package se.cs.eventsourcing.domain.aggregate.events;

import com.google.common.base.Preconditions;
import se.cs.eventsourcing.domain.store.event.DomainEvent;

import java.time.LocalDate;
import java.util.Optional;

public class NewPersonCreatedWithBirthdate implements DomainEvent {

    private final String firstName;
    private final String lastName;
    private final Optional<LocalDate> birthDate;

    public NewPersonCreatedWithBirthdate(String firstName,
                                         String lastName,
                                         Optional<LocalDate> birthDate) {

        this.firstName = Preconditions.checkNotNull(firstName);
        this.lastName = Preconditions.checkNotNull(lastName);
        this.birthDate = birthDate;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public Optional<LocalDate> getBirthDate() {
        return birthDate;
    }
}