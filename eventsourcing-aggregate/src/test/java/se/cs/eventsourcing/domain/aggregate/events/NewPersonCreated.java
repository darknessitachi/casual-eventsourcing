package se.cs.eventsourcing.domain.aggregate.events;

import com.google.common.base.Preconditions;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.event.EventHasUpgrade;

@EventHasUpgrade(NewPersonCreatedWithBirthdate.class)
public class NewPersonCreated implements DomainEvent {

    private final String firstName;
    private final String lastName;

    public NewPersonCreated(String firstName, String lastName) {
        this.firstName = Preconditions.checkNotNull(firstName);
        this.lastName = Preconditions.checkNotNull(lastName);
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }
}