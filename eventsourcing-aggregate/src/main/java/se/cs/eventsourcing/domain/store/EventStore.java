package se.cs.eventsourcing.domain.store;

import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An event store is a place where we store and load events.
 */
public interface EventStore {

    String newStream(List<DomainEvent> events, Set<Metadata> metadata);

    /**
     * Appends the provided events to the event stream
     * connected with the provided event stream id.
     *
     * @param command the create change set command contains
     *                all the stuff we need
     */
    void append(NewChangeSet command);

    /**
     * Loads a stream of events for the provided eventStreamId.
     * The result of this operation contains a sequence of events, ranging
     * from and including the version specified by the fromVersion argument
     * up to and including the version specified by the toVersion argument.
     *
     * @param eventStreamId the event steam id
     * @param toVersion the first event version to include in the stream
     * @param toVersion the last event version to include in the stream
     * @return the event stream if found, otherwise an empty result
     * @throws IllegalArgumentException if the event stream lacks any of
     * the provided versions, or if fromVersion > toVersion
     */
    Optional<EventStream> loadStream(String eventStreamId, long fromVersion, long toVersion);

    /**
     * Returns the most recent version of the aggregate with
     * the provided id
     *
     * @param eventStreamId the event stream id
     * @return the aggregate version or 0 if the aggregate does not exist
     */
    long getMostRecentVersion(String eventStreamId);
}
