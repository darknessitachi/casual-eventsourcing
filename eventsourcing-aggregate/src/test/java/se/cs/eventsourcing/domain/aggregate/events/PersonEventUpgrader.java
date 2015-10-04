package se.cs.eventsourcing.domain.aggregate.events;

import se.cs.eventsourcing.domain.store.event.EventUpgrader;

import java.util.Optional;

public class PersonEventUpgrader {

    @EventUpgrader
    public NewPersonCreatedWithBirthdate upgrade(NewPersonCreated event) {

        return new NewPersonCreatedWithBirthdate(
                event.getFirstName(),
                event.getLastName(),
                Optional.empty());
    }
}
