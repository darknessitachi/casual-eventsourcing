package se.cs.eventsourcing.domain.aggregate.events;

import com.google.common.base.Preconditions;
import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

public class NewPersonCreated extends AbstractDomainEvent {

    private String firstName;
    private String lastName;

    public NewPersonCreated(String firstName, String lastName) {
        super(1);
        this.firstName = Preconditions.checkNotNull(firstName);
        this.lastName = Preconditions.checkNotNull(lastName);
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }}