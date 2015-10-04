package se.cs.eventsourcing.domain.store.event;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;

public class DomainEventUpgradeServiceTest {


    @Test
    public void supportedDomainEventsTest() {
        Collection<Object> updateProviders = new ArrayList<>();
        updateProviders.add(new PersonDomainEventUpgrader());

        DomainEventUpgradeService service = new DomainEventUpgradeService(updateProviders);

        Set<Class<DomainEvent>> result =
                service.supportedDomainEvents(PersonDomainEventUpgrader.class);

        assertEquals(2, result.size());
        assertTrue(result.contains(NewAnimalCreated.class));
        assertTrue(result.contains(NewAnimalCreatedWithBirthdate.class));
    }

    @Test
    public void upgrade() {
        Collection<Object> updateProviders = new ArrayList<>();
        updateProviders.add(new PersonDomainEventUpgrader());

        DomainEventUpgradeService service = new DomainEventUpgradeService(updateProviders);

        DomainEvent event = new NewAnimalCreatedWithBirthdate("Polly", LocalDate.now());

        /**
         * This event needs one upgrade;
         * NewAnimalCreatedWithBirthdate -> NewAnimalCreatedWithZooName
         */
        DomainEvent upgraded = service.upgrade(event);

        assertEquals("The event has been upgraded",
                NewAnimalCreatedWithZooName.class, upgraded.getClass());
    }

    @Test
    public void upgradeRecursively() {
        Collection<Object> updateProviders = new ArrayList<>();
        updateProviders.add(new PersonDomainEventUpgrader());

        DomainEventUpgradeService service = new DomainEventUpgradeService(updateProviders);

        DomainEvent event = new NewAnimalCreated("Polly");

        /**
         * This event need two upgrades;
         * NewAnimalCreated -> NewAnimalCreatedWithBirthdate -> NewAnimalCreatedWithZooName
         */
        DomainEvent upgraded = service.upgrade(event);

        assertEquals("The event has been upgraded recursively",
                NewAnimalCreatedWithZooName.class, upgraded.getClass());
    }

    @Test
    public void upgradeEventNotInNeedOfAnUpgrade() {
        Collection<Object> updateProviders = new ArrayList<>();
        updateProviders.add(new PersonDomainEventUpgrader());

        DomainEventUpgradeService service = new DomainEventUpgradeService(updateProviders);

        DomainEvent event = new DomainEvent() {};

        DomainEvent result = service.upgrade(event);

        assertSame("No upgrade needed -> the same event is simply returned", event, result);
    }
}