package se.cs.eventsourcing.domain.aggregate;

import org.junit.Test;
import se.cs.eventsourcing.domain.aggregate.events.FirstNameChanged;
import se.cs.eventsourcing.domain.aggregate.events.LastNameChanged;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class AggregateTest {

    @Test
    public void create() {
        Person aggregate = new Person("Mister", "Bister");

        assertEquals(0, aggregate.version());
    }

    @Test
    public void isDirtyTest() {
        Person aggregate = new Person("Mister", "Bister");

        // the aggregate should be dirty as the constructor generates an event
        assertTrue(aggregate.isDirty());
        assertEquals("Changes have not yet been saved", 0, aggregate.version());

        // let's pretend that we save the changes..
        aggregate.clearChangesAndUpdateVersion();

        assertFalse(aggregate.isDirty());
        assertEquals("Changes have now been saved", 1, aggregate.version());

        // now add another change
        aggregate.changeFirstName("Moster");
        aggregate.changeLastName("Ester");

        // we should be dirty again
        assertTrue(aggregate.isDirty());
        assertEquals("Changes have not yet been saved", 1, aggregate.version());
    }

    @Test
    public void appendTest() {
        Person aggregate = new Person("Mister", "Bister");

        DomainEvent event = new FirstNameChanged("Moster");

        aggregate.append(event);

        assertTrue("the event should have been appended to the internal list of changes",
                aggregate.changes().contains(event));
    }

    @Test
    public void replay() {
        Person aggregate = new Person("Mister", "Bister");

        // now let's replay some events, 3 in total
        List<DomainEvent> events = new ArrayList<DomainEvent>();
        events.addAll(aggregate.changes());
        events.add(new FirstNameChanged("Moster"));
        events.add(new LastNameChanged("Ester"));

        // let's pretend that we are loading the stream with a total of 3
        // events, i.e. the event stream version is 3
        EventStream stream = EventStream.newFromStart(events);

        aggregate.replay(stream);

        assertEquals("Moster", aggregate.getFirstName());
        assertEquals("Ester", aggregate.getLastName());
        assertEquals(3, aggregate.version());
    }

    @Test(expected = DomainEventHandlerException.class)
    public void appendAnnotatedHandlerMethodIsMissing() {
        Aggregate aggregate = new Aggregate() {
            @Override
            public String getEventStreamId() {
                return "123";
            }
        };

        aggregate.append(TestObjectFactory.newSomethingChanged("something"));
    }

    @Test(expected = DomainEventHandlerException.class)
    public void replayAnnotatedHandlerMethodIsMissing() {
        Aggregate aggregate = new Aggregate() {
            @Override
            public String getEventStreamId() {
                return "123";
            }
        };

        aggregate.replay(
                EventStream.newFromStart(
                        Collections.singletonList(TestObjectFactory.newSomethingChanged("something"))));
    }

    @Test
    public void sameId() {
        Aggregate aggregateA = new ForEqualsTest("123");
        Aggregate aggregateB = new ForEqualsTest("123");

        assertEquals(aggregateA, aggregateB);
    }

    @Test
    public void differentId() {
        Aggregate aggregateA = new ForEqualsTest("123");
        Aggregate aggregateB = new ForEqualsTest("321");

        assertNotEquals(aggregateA, aggregateB);
    }

    static class ForEqualsTest extends Aggregate {

        private String id;

        ForEqualsTest(String id) {
            this.id = id; // this is not the normal way to do it..
        }

        @Override
        public String getEventStreamId() {
            return id;
        }
    }
 }