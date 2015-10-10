package se.cs.eventsourcing.domain.store.metadata;

import org.junit.Test;
import se.cs.eventsourcing.domain.changeset.KnownMetadata;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.changeset.Metadatum;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;

public class MetadataTest {

    @Test
    public void withWhen() {
        ZonedDateTime now = ZonedDateTime.now();
        Metadatum when = Metadata.withWhen(now);

        assertEquals(KnownMetadata.WHEN.getKey(), when.getKey());
        assertEquals(now.format(DateTimeFormatter.ISO_INSTANT), when.getValue());
    }

    @Test
    public void withWhenNow() {
        Metadatum when = Metadata.withWhenNow();

        assertEquals(KnownMetadata.WHEN.getKey(), when.getKey());
        assertNotNull(when.getValue());
    }

    @Test
    public void withUserReference() {
        Metadatum userRef = Metadata.withUserReference("123");

        assertEquals(KnownMetadata.USER_REFERENCE.getKey(), userRef.getKey());
        assertEquals("123", userRef.getValue());
    }

    @Test
    public void withCustomMetadata() {
        Metadatum custom = Metadata.withMetadata("somekey", "asdf");

        assertEquals("somekey", custom.getKey());
        assertEquals("asdf", custom.getValue());
    }
}