package se.cs.eventsourcing.domain.store.changeset;

import se.cs.eventsourcing.domain.store.DomainEvent;

import java.util.Objects;

public class StoredEvent {

    private final long id;
    private final String eventStreamId;
    private final DomainEvent event;

    public StoredEvent(long id, String eventStreamId, DomainEvent event) {
        this.id = id;
        this.eventStreamId = eventStreamId;
        this.event = event;
    }

    public long getId() {
        return id;
    }

    public String getEventStreamId() {
        return eventStreamId;
    }

    public DomainEvent getEvent() {
        return event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredEvent that = (StoredEvent) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(eventStreamId, that.eventStreamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventStreamId);
    }
}
