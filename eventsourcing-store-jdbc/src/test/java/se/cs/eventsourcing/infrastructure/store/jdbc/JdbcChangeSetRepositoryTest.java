package se.cs.eventsourcing.infrastructure.store.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import se.cs.eventsourcing.domain.store.changeset.ChangeSet;
import se.cs.eventsourcing.domain.store.event.DomainEvent;
import se.cs.eventsourcing.domain.store.metadata.Metadata;
import se.cs.eventsourcing.domain.store.metadata.Metadatum;
import se.cs.eventsourcing.infrastructure.store.jdbc.sample.ChangeFirstName;
import se.cs.eventsourcing.infrastructure.store.jdbc.sample.ChangeLastName;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:application-context.xml" })
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class JdbcChangeSetRepositoryTest {

    @Autowired
    private JdbcEventStore store;

    @Autowired
    private JdbcChangeSetRepository instance;

    private JdbcTemplate template;

    @Test
    public void getChangeSets() {
        String streamId = sampleStream();

        List<ChangeSet> result = instance.getChangeSets(streamId);

        assertEquals(1, result.size());

        ChangeSet changeSet = result.get(0);

        assertEquals(streamId, changeSet.getEventStreamId());
        assertEquals(2, changeSet.getStoredEvents().size());
        assertEquals(1, changeSet.getMetadata().size());
    }

    @Test
    public void getChangeSetById() {
        String streamId = sampleStream();

        List<ChangeSet> result = instance.getChangeSets(streamId);
        String changeSetId = result.get(0).getId();

        assertTrue(instance.getChangeSetById(changeSetId).isPresent());
    }

    private String sampleStream() {
        List<DomainEvent> events = new ArrayList<>();

        events.add(new ChangeFirstName("First"));
        events.add(new ChangeLastName("Last"));

        Metadatum now = Metadata.withWhenNow();

        Set<Metadatum> metadata = new HashSet<>();
        metadata.add(now);

        return store.newStream(events, metadata);
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }
}