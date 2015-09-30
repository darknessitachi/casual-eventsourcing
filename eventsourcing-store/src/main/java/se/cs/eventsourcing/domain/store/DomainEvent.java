package se.cs.eventsourcing.domain.store;

/**
 * A domain event is something that has happened in the past.
 */
public interface DomainEvent {

    /**
     * The version of the event. If the event structure ever needs to
     * change this construct will make it possible to communicate that
     * we now have a new version to cope with.
     */
    int eventVersion();
}
