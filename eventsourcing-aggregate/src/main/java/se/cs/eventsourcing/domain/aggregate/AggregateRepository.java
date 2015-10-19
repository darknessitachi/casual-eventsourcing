package se.cs.eventsourcing.domain.aggregate;

import org.reflections.ReflectionUtils;
import se.cs.eventsourcing.domain.changeset.KnownMetadata;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.store.EventStore;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Aggregate repository that works with the underlying
 * event store implementation to append to and load
 * from streams. Subclass and enjoy!
 *
 * @param <T> the aggregate type
 */
public abstract class AggregateRepository<T extends Aggregate> {

    /**
     * The underlying event store
     */
    private EventStore store;

    public AggregateRepository(EventStore store) {
        this.store = checkNotNull(store);
    }

    /**
     * Attempts to save the aggregate by appending the internal
     * list of changes in the aggregate to the corresponding event
     * stream in the event store. The save operation is not idempotent -
     * the aggregate will be emptied of its events and updated to the
     * latest version if the save operation is successful.
     *
     * @param aggregate the aggregate whose changes we want to save
     */
    public void save(T aggregate, Metadata... metadata) {
        if (!aggregate.isDirty()) {
            return;
        }

        Set<Metadata> metadataSet = assembleMetadata(metadata);

        if (aggregate.getEventStreamId() == null) {
            String newId = store.newStream(aggregate.changes(), metadataSet);
            setEventStreamId(aggregate, newId);
        } else {
            store.append(
                    new NewChangeSet(aggregate.getEventStreamId(),
                            aggregate.version(),
                            aggregate.changes(),
                            metadataSet));
        }

        aggregate.clearChangesAndUpdateVersion();
    }

    private void setEventStreamId(T aggregate, String id) {
        Set<Field> fields =
                ReflectionUtils.getFields(getAggregateClass(),
                        ReflectionUtils.withAnnotation(EventStreamId.class));

        if (fields.size() != 1) {
            throw new RuntimeException(
                    String.format("No aggregate id field found in class %s", getAggregateClass()));
        }

        Field first = fields.iterator().next();

        try {
            first.setAccessible(true);
            first.set(aggregate, id);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    String.format("Could not set aggregate id on class %s", getAggregateClass()));
        }
    }

    private Set<Metadata> assembleMetadata(Metadata[] metadata) {
        Set<Metadata> result = new HashSet<>();

        Collections.addAll(result, metadata);

        boolean dateIsSet =
                result.stream().anyMatch(
                        md -> md.getKey().equals(KnownMetadata.WHEN.getKey()));

        if (!dateIsSet) {
            result.add(Metadata.withWhen());
        }

        return result;
    }


    /**
     * Looks up the latest version of the aggregate by its id. The result
     * is wrapped in an {@link Optional} container where an empty result
     * indicates that no aggregate with the provided id exists in the
     * event store.
     *
     * @param eventStreamId the aggregate id
     * @return an optional container with the result embedded (if existing)
     */
    public Optional<T> find(String eventStreamId) {
        return find(eventStreamId, store.getMostRecentVersion(eventStreamId));
    }

    public Optional<T> find(String eventStreamId, long aggregateVersion) {
        Optional<EventStream> stream =
                store.loadStream(eventStreamId, 1, aggregateVersion);

        return stream.isPresent()
                ? Optional.of(createAndReplayAggregate(eventStreamId, stream.get()))
                : Optional.<T>empty();
    }

    @SuppressWarnings("unchecked")
    private T createAndReplayAggregate(String eventStreamId, EventStream stream) {

        Set<Constructor> result =
                ReflectionUtils.getConstructors(getAggregateClass(), ReflectionUtils.withParameters());

        if (result.size() != 1) {
            throw new DomainEventHandlerException("Only one no-arg constructor allowed for aggregates.");
        }

        try {
            Constructor first = result.iterator().next();
            first.setAccessible(true);

            T object = (T) first.newInstance();

            object.replay(stream);

            setEventStreamId(object, eventStreamId);

            return object;
        } catch (Exception e) {
            throw new DomainEventHandlerException(e);
        }
    }

    protected abstract Class<? extends T> getAggregateClass();
}
