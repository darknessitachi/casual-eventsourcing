package se.cs.eventsourcing.domain.aggregate;

import se.cs.eventsourcing.domain.aggregate.events.FirstNameChanged;
import se.cs.eventsourcing.domain.aggregate.events.LastNameChanged;
import se.cs.eventsourcing.domain.aggregate.events.NewPersonCreated;

public class Person extends Aggregate {

    @EventStreamId
    private String id;

    private String firstName;
    private String lastName;

    private Person() {} // for replay purposes

    public Person(String firstName, String lastName) {
        NewPersonCreated event = new NewPersonCreated(firstName, lastName);
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

    @DomainEventHandler
    private void apply(FirstNameChanged event) {
        this.firstName = event.getFirstName();
    }

    @DomainEventHandler
    private void apply(LastNameChanged event) {
        this.lastName = event.getLastName();
    }

    @DomainEventHandler
    private void apply(NewPersonCreated event) {
        this.firstName = event.getFirstName();
        this.lastName = event.getLastName();
    }

    @Override
    public String getEventStreamId() {
        return id;
    }
}
