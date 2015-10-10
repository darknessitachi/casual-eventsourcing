package se.cs.eventsourcing.infrastructure.store.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.changeset.Metadatum;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;
import se.cs.eventsourcing.infrastructure.store.jdbc.sample.ChangeFirstName;
import se.cs.eventsourcing.infrastructure.store.jdbc.sample.ChangeLastName;

import javax.sql.DataSource;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:application-context.xml" })
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class JdbcEventStoreTest {

    @Autowired
    private JdbcEventStore instance;

    private JdbcTemplate template;

    @Test
    public void newStream() {
        List<DomainEvent> events = new ArrayList<>();

        events.add(new ChangeFirstName("First"));
        events.add(new ChangeLastName("Last"));

        Metadatum now = Metadata.withWhenNow();

        Set<Metadatum> metadata = new HashSet<>();
        metadata.add(now);

        String id = instance.newStream(events, metadata);

        EventStream stream = instance.loadStream(id).get();

        assertEquals("Two events were added",
                2, stream.getEvents().size());

        ChangeFirstName event1 = (ChangeFirstName) stream.getEvents().get(0);
        ChangeLastName event2 = (ChangeLastName) stream.getEvents().get(1);

        assertEquals("First", event1.getName());
        assertEquals("Last", event2.getName());
        assertEquals(1, stream.fromVersion());
        assertEquals(2, stream.toVersion());
    }

    @Test
    public void append() {
        String id = sampleStream();
        EventStream stream = instance.loadStream(id).get();

        assertEquals(2, stream.toVersion());

        List<DomainEvent> events = new ArrayList<>();
        events.add(new ChangeFirstName("Changed"));

        instance.append(new NewChangeSet(id,
                stream.toVersion(),
                events,
                Collections.emptySet()));

        stream = instance.loadStream(id).get();

        assertEquals("One more event was added. Two plus one equals three.",
                3, stream.toVersion());

        ChangeFirstName third = (ChangeFirstName) stream.getEvents().get(2);

        assertEquals("Changed", third.getName());
    }

    @Test
    public void getMostRecentVersion() {
        String id = "8fe58442-22dc-4094-a759-105e772fc67d";

        assertEquals("No such stream means version 0",
                0, instance.getMostRecentVersion(id));

        id = sampleStream();

        assertEquals("Two events were added, version should be 2",
                2, instance.getMostRecentVersion(id));
    }

    private String sampleStream() {
        List<DomainEvent> events = new ArrayList<>();

        events.add(new ChangeFirstName("First"));
        events.add(new ChangeLastName("Last"));

        Metadatum now = Metadata.withWhenNow();

        Set<Metadatum> metadata = new HashSet<>();
        metadata.add(now);

        return instance.newStream(events, metadata);
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }
}