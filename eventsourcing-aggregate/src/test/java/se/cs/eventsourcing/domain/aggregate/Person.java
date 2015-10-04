package se.cs.eventsourcing.domain.aggregate;

import se.cs.eventsourcing.domain.aggregate.events.FirstNameChanged;
import se.cs.eventsourcing.domain.aggregate.events.LastNameChanged;
import se.cs.eventsourcing.domain.aggregate.events.NewPersonCreated;
import se.cs.eventsourcing.domain.aggregate.events.NewPersonCreatedWithBirthdate;

import java.time.LocalDate;
import java.util.Optional;

public class Person extends Aggregate {

    @EventStreamId
    private String id;

    private String firstName;
    private String lastName;
    private Optional<LocalDate> birthDate;

    private Person() {} // for replay purposes

    public Person(String firstName, String lastName, Optional<LocalDate> birthDate) {
        NewPersonCreatedWithBirthdate event =
                new NewPersonCreatedWithBirthdate(firstName, lastName, birthDate);

        apply(event);
        append(event);
    }

    public void changeFirstName(String newValue) {
        FirstNameChanged event = new FirstNameChanged(newValue);
        apply(event);
        append(event);
    }

    public void changeLastName(String newValue) {
        LastNameChanged event = new LastNameChanged(newValue);
        apply(event);
        append(event);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Optional<LocalDate> getBirthDate() {
        return birthDate;
    }

    @DomainEventHandler
    private void apply(FirstNameChanged event) {
        this.firstName = event.getFirstName();
    }

    @DomainEventHandler
    private void apply(LastNameChanged event) {
        this.lastName = event.getLastName();
    }

    @DomainEventHandler
    private void apply(NewPersonCreatedWithBirthdate event) {
        this.firstName = event.getFirstName();
        this.lastName = event.getLastName();
        this.birthDate = event.getBirthDate();
    }
}
