package se.cs.eventsourcing.infrastructure.store;

import org.junit.Test;
import se.cs.eventsourcing.domain.changeset.ChangeSet;
import se.cs.eventsourcing.domain.changeset.KnownMetadata;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.TestObjectFactory;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InMemoryEventStoreTest {

    private InMemoryEventStore instance = new InMemoryEventStore();

    @Test
    public void append() {
        String eventStreamId = newEventStream();
        long expectedVersion = instance.getMostRecentVersion(eventStreamId);

        List<DomainEvent> events = new ArrayList<DomainEvent>();
        events.add(TestObjectFactory.newSomethingChanged("Something else"));

        Set<Metadata> metadata = new HashSet<>();
        metadata.add(Metadata.withUserReference("54321"));

        instance.append(
                new NewChangeSet(eventStreamId, expectedVersion, events, metadata));

        // let's look the events up
        Optional<EventStream> result =
                instance.loadStream(eventStreamId, 1, instance.getMostRecentVersion(eventStreamId));

        assertTrue(result.isPresent());

        EventStream stream = result.get();

        // we used to have one event, we now have two
        assertEquals(2, stream.getEvents().size());
        assertTrue(stream.getEvents().contains(events.get(0)));

        // the stream version equals the number of events in the store
        assertEquals(2, stream.toVersion());
    }


    @Test(expected = IllegalStateException.class)
    public void appendWrongVersion() {
        String eventStreamId = newEventStream();

        List<DomainEvent> events = new ArrayList<DomainEvent>();
        events.add(TestObjectFactory.newSomethingChanged("Something else"));

        Set<Metadata> metadata = new HashSet<>();
        metadata.add(Metadata.withUserReference("54321"));

        // boom!
        instance.append(
                new NewChangeSet(eventStreamId, 123, events, metadata));
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadStreamIllegalFromVersion() {
        String eventStreamId = newEventStream();

        instance.loadStream(eventStreamId, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadStreamIllegalToVersion() {
        String eventStreamId = newEventStream();

        instance.loadStream(eventStreamId, 1, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadStreamFromVersionLargerThanToVersion() {
        String eventStreamId = newEventStream();

        instance.loadStream(eventStreamId, 2, 1);
    }

    @Test
    public void loadStreamOldVersion() {
        List<DomainEvent> events = new ArrayList<DomainEvent>();
        events.add(TestObjectFactory.newSomethingChanged("Something"));
        events.add(TestObjectFactory.newSomethingChanged("Something else"));
        events.add(TestObjectFactory.newSomethingChanged("Yet more things happened"));

        String eventStreamId = instance.newStream(events, Collections.emptySet());

        // let's fetch the two first events
        EventStream stream = instance.loadStream(eventStreamId, 1, 2).get();

        assertEquals(1, stream.fromVersion());
        assertEquals(2, stream.toVersion());
        assertEquals("Something",
                ((TestObjectFactory.SomethingChanged)stream.getEvents().get(0)).getSomething());
        assertEquals("Something else",
                ((TestObjectFactory.SomethingChanged)stream.getEvents().get(1)).getSomething());

        // let's fetch the two last events
        stream = instance.loadStream(eventStreamId, 2, 3).get();

        assertEquals(2, stream.fromVersion());
        assertEquals(3, stream.toVersion());
        assertEquals("Something else",
                ((TestObjectFactory.SomethingChanged)stream.getEvents().get(0)).getSomething());
        assertEquals("Yet more things happened",
                ((TestObjectFactory.SomethingChanged)stream.getEvents().get(1)).getSomething());
    }

    @Test
    public void getChangeSets() {
        String eventStreamId = newEventStream();

        // so there should be one change set containing one event..
        List<ChangeSet> changeSets = instance.getChangeSets(eventStreamId);

        ChangeSet changeSet = changeSets.get(0);

        assertEquals(eventStreamId, changeSet.getEventStreamId());
        assertEquals(1, changeSet.getMetadata().size());

        // user metadata
        assertEquals("12345",
                changeSet.getMetadata().get(
                        KnownMetadata.USER_REFERENCE.getKey()).getValue());
    }

    @Test
    public void getChangeSetById() {
        String eventStreamId = newEventStream();

        ChangeSet changeSet = instance.getChangeSets(eventStreamId).get(0);

        Optional<ChangeSet> result =
                instance.getChangeSetById(changeSet.getId());

        assertTrue(result.isPresent());
    }

    @Test
    public void newStreamWithSubscriber() {

        SomeSubscriber subscriber = new SomeSubscriber();

        List<DomainEvent> events = new ArrayList<>();
        events.add(TestObjectFactory.newSomethingChanged("Something"));

        instance.subscribe(subscriber);
        instance.newStream(events, Collections.emptySet());

        assertTrue(subscriber.wasCalled());
    }

    @Test
    public void appendWithSubscriber() {
        SomeSubscriber subscriber = new SomeSubscriber();
        instance.subscribe(subscriber);

        String aggregateId = newEventStream();
        long expectedVersion = instance.getMostRecentVersion(aggregateId);

        List<DomainEvent> events = new ArrayList<DomainEvent>();
        events.add(TestObjectFactory.newSomethingChanged("Something else"));

        Set<Metadata> metadata = new HashSet<>();
        metadata.add(Metadata.withUserReference("54321"));

        instance.append(
                new NewChangeSet(aggregateId, expectedVersion, events, metadata));

        assertTrue(subscriber.wasCalled());
    }

    private String newEventStream() {
        List<DomainEvent> events = new ArrayList<DomainEvent>();
        events.add(TestObjectFactory.newSomethingChanged("Something"));

        Set<Metadata> metadata = new HashSet<>();
        metadata.add(Metadata.withUserReference("12345"));

        return instance.newStream(events, metadata);
    }

    static class SomeSubscriber implements EventSubscriber {

        private boolean wasCalled;

        @Override
        public void handle(ChangeSet changeSet) {
            wasCalled = true;
        }

        boolean wasCalled() {
            return wasCalled;
        }
    }
}