package se.cs.eventsourcing.domain.store;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class EventStreamTest {

    @Test
    public void newFromStart() {
        List<DomainEvent> events = new ArrayList<>();
        events.add(TestObjectFactory.newSomethingChanged("something"));
        events.add(TestObjectFactory.newSomethingChanged("something else"));

        EventStream stream = EventStream.newFromStart(events);

        assertEquals(1, stream.fromVersion());
        assertEquals(2, stream.toVersion());
        assertEquals(2, stream.getEvents().size());
    }

    @Test
    public void newFromVersion() {
        List<DomainEvent> events = new ArrayList<>();
        events.add(TestObjectFactory.newSomethingChanged("something"));
        events.add(TestObjectFactory.newSomethingChanged("something else"));

        EventStream stream = EventStream.newFromVersion(3, events);

        assertEquals(3, stream.fromVersion());
        assertEquals(4, stream.toVersion());
    }
}