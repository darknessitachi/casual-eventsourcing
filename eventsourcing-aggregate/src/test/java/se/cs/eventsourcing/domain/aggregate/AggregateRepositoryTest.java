package se.cs.eventsourcing.domain.aggregate;

import org.junit.Test;
import se.cs.eventsourcing.infrastructure.store.InMemoryEventStore;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.*;

public class AggregateRepositoryTest {

    private String anEventStreamId;

    @Test
    public void find() {
        PersonRepository rep = newRepositoryWithAnAggregate();

        Optional<Person> result = rep.find(anEventStreamId);

        assertTrue(result.isPresent());

        Person person = result.get();

        assertEquals(anEventStreamId, person.getEventStreamId());
    }

    @Test
    public void findParticularVersion() {
        PersonRepository rep = newRepositoryWithAnAggregate();

        Person person = rep.find(anEventStreamId).get();

        person.changeLastName("Last name");
        person.changeFirstName("First name");

        rep.save(person);

        // okay, we should now be at version three
        assertEquals(3, person.version());
        assertEquals("First name", person.getFirstName());

        // let's fetch an old version of the aggregate
        person = rep.find(anEventStreamId, 2).get();

        assertEquals(2, person.version());
        assertEquals("Martin", person.getFirstName());
    }

    @Test
    public void findNoResult() {
        assertFalse(newRepositoryWithAnAggregate().find("asdf").isPresent());
    }

    @Test
    public void save() {
        PersonRepository rep =
                new PersonRepository(new InMemoryEventStore());

        LocalDate birthDate = LocalDate.of(1979, 10, 9);
        Person person = new Person("Martin", "Moberg", Optional.of(birthDate));

        assertNull(person.getEventStreamId());
        assertEquals("Fresh instance means version 0",
                0, person.version());

        assertTrue(person.isDirty());

        // let's save the person..
        rep.save(person);

        assertNotNull(person.getEventStreamId());
        assertFalse(person.isDirty());
        assertEquals("Version should have increased by one",
                1, person.version());

        // let's fetch the stored person
        Person stored = rep.find(person.getEventStreamId()).get();

        // the updates should have been applied to the person instance
        assertNotNull(stored.getEventStreamId());
        assertEquals(person.getFirstName(), stored.getFirstName());
        assertEquals(person.getLastName(), stored.getLastName());
        assertEquals(1, stored.version());
    }

    @Test(expected = IllegalStateException.class)
    public void saveOldVersion() {
        PersonRepository rep = newRepositoryWithAnAggregate();

        Person person = rep.find(anEventStreamId).get();

        person.changeLastName("Last name");
        person.changeFirstName("First name");

        rep.save(person);

        // version is now three - let's load the previous version
        person = rep.find(anEventStreamId, 2).get();

        // okay, now we add a change and try to save it
        person.changeFirstName("Old version first name");

        // this should throw an exception; Expected version 2, got 3
        rep.save(person);
    }

    private PersonRepository newRepositoryWithAnAggregate() {
        PersonRepository rep =
                new PersonRepository(new InMemoryEventStore());

        LocalDate birthDate = LocalDate.of(1979, 10, 9);
        Person person = new Person("Martin", "Moberg", Optional.of(birthDate));

        rep.save(person);
        anEventStreamId = person.getEventStreamId();

        return rep;
    }
}