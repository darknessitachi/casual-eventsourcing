package se.cs.eventsourcing.domain.store;

import se.cs.eventsourcing.domain.changeset.Metadatum;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An event store is a place where we store and load events.
 */
public interface EventStore {

    String newStream(List<DomainEvent> events, Set<Metadatum> metadata);

    /**
     * Appends the provided events to the event stream
     * connected with the provided event stream id.
     *
     * @param command the create change set command contains
     *                all the stuff we need
     */
    void append(NewChangeSet command);

    /**
     * Loads the stream of events for the provided eventStreamId.
     * TODO: there should be some way to limit or page this..
     *
     * @param eventStreamId the event steam id
     * @return the event stream if found, otherwise an empty result
     */
    Optional<EventStream> loadStream(String eventStreamId);

    /**
     * Returns the most recent version of the aggregate with
     * the provided id
     *
     * @param eventStreamId the event stream id
     * @return the aggregate version or 0 if the aggregate does not exist
     */
    long getMostRecentVersion(String eventStreamId);
}
