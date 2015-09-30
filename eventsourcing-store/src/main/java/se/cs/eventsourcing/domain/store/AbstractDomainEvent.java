package se.cs.eventsourcing.domain.store;

import java.util.Objects;

/**
 * Convenient base implementation for domain events to avoid
 * repeating stuff.
 */
public abstract class AbstractDomainEvent implements DomainEvent {

    private final int eventVersion;

    public AbstractDomainEvent(int eventVersion) {
        this.eventVersion = eventVersion;
    }

    public int eventVersion() {
        return eventVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDomainEvent that = (AbstractDomainEvent) o;
        return Objects.equals(eventVersion, that.eventVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventVersion);
    }
}
