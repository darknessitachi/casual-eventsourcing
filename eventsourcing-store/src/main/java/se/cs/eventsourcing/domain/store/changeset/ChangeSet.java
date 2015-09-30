package se.cs.eventsourcing.domain.store.changeset;

import se.cs.eventsourcing.domain.store.metadata.Metadatum;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChangeSet {
    private final long id;
    private final String eventStreamId;
    private final List<StoredEvent> storedEvents;
    private final Map<String, Metadatum> metadata;

    public ChangeSet(long id,
                     String eventStreamId,
                     List<StoredEvent> storedEvents,
                     Map<String, Metadatum> metadata) {

        this.id = id;
        this.eventStreamId = eventStreamId;
        this.storedEvents = storedEvents;
        this.metadata = metadata;
    }

    public long getId() {
        return id;
    }

    public String getEventStreamId() {
        return eventStreamId;
    }

    public List<StoredEvent> getStoredEvents() {
        return storedEvents;
    }

    public Map<String, Metadatum> getMetadata() {
        return metadata;
    }

    public boolean containsEvent(long eventId) {
        for (StoredEvent storedEvent : storedEvents) {
            if (storedEvent.getId() == eventId) {
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
}
