package se.cs.eventsourcing.domain.store.changeset;

import se.cs.eventsourcing.domain.store.event.DomainEvent;
import se.cs.eventsourcing.domain.store.metadata.Metadatum;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class NewChangeSet {

    private final String eventStreamId;
    private final long expectedVersion;
    private final List<DomainEvent> events;
    private final Set<Metadatum> metadata;

    public NewChangeSet(String eventStreamId, long expectedVersion,
                        List<DomainEvent> events, Set<Metadatum> metadata) {

        checkArgument(expectedVersion > 0);

        this.eventStreamId = checkNotNull(eventStreamId);
        this.expectedVersion = expectedVersion;
        this.events = new ArrayList<>(events);
        this.metadata = new HashSet<>(metadata);
    }

    public String getEventStreamId() {
        return eventStreamId;
    }

    public long getExpectedVersion() {
        return expectedVersion;
    }

    public List<DomainEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public Set<Metadatum> getMetadata() {
        return Collections.unmodifiableSet(metadata);
    }
}
