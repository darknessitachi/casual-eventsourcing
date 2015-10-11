package se.cs.eventsourcing.domain.changeset;

import se.cs.eventsourcing.domain.event.StoredEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ChangeSet {
    private String id;
    private String eventStreamId;
    private List<StoredEvent> storedEvents;
    private Map<String, Metadata> metadata;

    private ChangeSet() {}

    public ChangeSet(String id,
                     String eventStreamId,
                     List<StoredEvent> storedEvents,
                     Map<String, Metadata> metadata) {

        this.id = checkNotNull(id);
        this.eventStreamId = checkNotNull(eventStreamId);
        this.storedEvents = checkNotNull(storedEvents);
        this.metadata = checkNotNull(metadata);

        checkArgument(!storedEvents.isEmpty());
    }

    public ChangeSet(String id,
                     String eventStreamId,
                     List<StoredEvent> storedEvents) {

        this(id, eventStreamId, storedEvents, new HashMap<>());
    }

    public String getId() {
        return id;
    }

    public String getEventStreamId() {
        return eventStreamId;
    }

    public List<StoredEvent> getStoredEvents() {
        return storedEvents;
    }

    public Map<String, Metadata> getMetadata() {
        return metadata;
    }

    public boolean containsEvent(String eventId) {
        for (StoredEvent storedEvent : storedEvents) {
            if (storedEvent.getId().equals(eventId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeSet changeSet = (ChangeSet) o;
        return Objects.equals(id, changeSet.id) &&
                Objects.equals(eventStreamId, changeSet.eventStreamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventStreamId);
    }

    @Override
    public String toString() {
        return "ChangeSet{" +
                "id='" + id + '\'' +
                ", eventStreamId='" + eventStreamId + '\'' +
                ", storedEvents=" + storedEvents +
                ", metadata=" + metadata +
                '}';
    }
}
