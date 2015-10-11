package se.cs.eventsourcing.domain.store.metadata;

import org.junit.Test;
import se.cs.eventsourcing.domain.changeset.KnownMetadata;
import se.cs.eventsourcing.domain.changeset.Metadata;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetadataTest {

    @Test
    public void withWhen() {
        ZonedDateTime now = ZonedDateTime.now();
        Metadata when = Metadata.withWhen(now);

        assertEquals(KnownMetadata.WHEN.getKey(), when.getKey());
        assertEquals(now.format(DateTimeFormatter.ISO_INSTANT), when.getValue());
    }

    @Test
    public void withWhenNow() {
        Metadata when = Metadata.withWhen();

        assertEquals(KnownMetadata.WHEN.getKey(), when.getKey());
        assertNotNull(when.getValue());
    }

    @Test
    public void withUserReference() {
        Metadata userRef = Metadata.withUserReference("123");

        assertEquals(KnownMetadata.USER_REFERENCE.getKey(), userRef.getKey());
        assertEquals("123", userRef.getValue());
    }

    @Test
    public void withCustomMetadata() {
        Metadata custom = new Metadata("somekey", "asdf");

        assertEquals("somekey", custom.getKey());
        assertEquals("asdf", custom.getValue());
    }
}