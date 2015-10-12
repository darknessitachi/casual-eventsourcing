package se.cs.eventsourcing.domain.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

@JsonDeserialize(using = StoredEventDeserializer.class)
public class StoredEvent {

    private String id;
    private String eventStreamId;
    private DomainEvent event;
    private String canonicalName;

    private StoredEvent() {}

    public StoredEvent(String id, String eventStreamId, DomainEvent event) {
        this.id = id;
        this.eventStreamId = eventStreamId;
        this.event = event;
        this.canonicalName = event.getClass().getCanonicalName();
    }

    public String getId() {
        return id;
    }

    public String getEventStreamId() {
        return eventStreamId;
    }

    public DomainEvent getEvent() {
        return event;
    }

    public String getCanonicalName() {
        return canonicalName;
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

    @Override
    public String toString() {
        return "StoredEvent{" +
                "id='" + id + '\'' +
                ", eventStreamId='" + eventStreamId + '\'' +
                ", event=" + event +
                ", canonicalName='" + canonicalName + '\'' +
                '}';
    }
}
