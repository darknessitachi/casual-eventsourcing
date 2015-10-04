package se.cs.eventsourcing.domain.store.upgrade;

import java.time.LocalDate;

public class PersonDomainEventUpgrader {

    @EventUpgrader
    public NewAnimalCreatedWithBirthdate upgrade(NewAnimalCreated event) {
        // let's pretend that we looked up the animal birth date
        LocalDate lookedUpPersonBirthDate = LocalDate.of(1979, 10 ,9);

        return new NewAnimalCreatedWithBirthdate(
                event.getName(),
                lookedUpPersonBirthDate);
    }

    @EventUpgrader
    public NewAnimalCreatedWithZooName upgrade(NewAnimalCreatedWithBirthdate event) {
        // let's pretend that we looked up the animal birth date
        LocalDate lookedUpPersonBirthDate = LocalDate.of(1979, 10 ,9);

        // The zoo name is looked up from somewhere..
        String zooName = "One two zoo";

        return new NewAnimalCreatedWithZooName(
                event.getName(),
                lookedUpPersonBirthDate,
                zooName);
    }
}
