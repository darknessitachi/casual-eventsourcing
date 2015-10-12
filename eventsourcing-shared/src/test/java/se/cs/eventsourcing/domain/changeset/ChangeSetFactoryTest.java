package se.cs.eventsourcing.domain.changeset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import se.cs.eventsourcing.domain.event.StoredEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChangeSetFactoryTest {

    @Test
    public void create() {
        String serialized = "{\"id\":\"changeSetId\",\"eventStreamId\":\"eventStreamId\",\"storedEvents\":[{\"id\":\"storedEventId\",\"eventStreamId\":\"eventStreamId\",\"event\":{\"someString\":\"someString\",\"someInt\":2},\"canonicalName\":\"se.cs.eventsourcing.domain.changeset.SomeEvent\"}],\"metadata\":{\"casual.eventsourcing.metadata.userreference\":{\"key\":\"casual.eventsourcing.metadata.userreference\",\"value\":\"someUser\"}}}";

        ChangeSetFactory factory = new ChangeSetFactory(new ObjectMapper());

        ChangeSet result = factory.create(serialized);

        assertEquals("changeSetId", result.getId());
        assertEquals("eventStreamId", result.getEventStreamId());
        assertEquals(1, result.getStoredEvents().size());

        StoredEvent storedEvent = result.getStoredEvents().get(0);
        assertEquals("storedEventId", storedEvent.getId());
        assertEquals("eventStreamId", storedEvent.getEventStreamId());
        assertEquals(SomeEvent.class.getCanonicalName(), storedEvent.getCanonicalName());

        SomeEvent event = (SomeEvent) storedEvent.getEvent();
        assertEquals(2, event.getSomeInt());
        assertEquals("someString", event.getSomeString());

        assertEquals(1, result.getMetadata().size());
        assertTrue(result.getMetadata().containsKey(KnownMetadata.USER_REFERENCE.getKey()));

        Metadata entry = result.getMetadata().get(KnownMetadata.USER_REFERENCE.getKey());
        assertEquals(KnownMetadata.USER_REFERENCE.getKey(), entry.getKey());
        assertEquals("someUser", entry.getValue());
    }
}