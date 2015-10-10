package se.cs.eventsourcing.infrastructure.store;

import org.junit.Before;
import org.junit.Test;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.EventStore;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.event.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EventStoreWithEventUpgradesTest {

    private EventStoreWithEventUpgrades instance;


    @Before
    public void setup() {
        EventUpgradeService service =
                new EventUpgradeService(
                        Collections.singleton(new AnimalEventUpgrader()));

        EventStore store = new EventStoreWithEventUpgrades(new InMemoryEventStore(), service);

        this.instance = (EventStoreWithEventUpgrades) store;
    }

    @Test
    public void loadStreamWithUpgrades() {
        // first let's create an event stream that contain upgradable events
        List<DomainEvent> events = new ArrayList<>();

        // this event has two upgrades pending
        events.add(new NewAnimalCreated("Polly"));

        // mistook a parrot for an elephant..this event has no upgrades
        events.add(new ChangeName("Dumbo"));

        String eventStreamId = instance.newStream(events, Collections.emptySet());

        EventStream es = instance.loadStream(eventStreamId).get();

        DomainEvent firstEvent = es.getEvents().get(0);
        assertEquals("the first event should have been upgraded",
                NewAnimalCreatedWithZooName.class, firstEvent.getClass());

        DomainEvent secondEvent = es.getEvents().get(1);
        assertEquals("the second event should have been left untouched",
                ChangeName.class, secondEvent.getClass());

    }
}