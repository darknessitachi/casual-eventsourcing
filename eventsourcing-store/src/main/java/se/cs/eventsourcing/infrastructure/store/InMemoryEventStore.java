package se.cs.eventsourcing.infrastructure.store;

import se.cs.eventsourcing.domain.changeset.ChangeSet;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.event.StoredEvent;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.changeset.ChangeSetRepository;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A basic in memory event store, just for fun!
 */
public class InMemoryEventStore extends EventPublishingStore implements ChangeSetRepository {

    private final Map<String, List<ChangeSet>> store =new HashMap<>();

    private final Map<String, List<StoredEvent>> flattenedStore = new HashMap<>();

    @Override
    public Optional<EventStream> loadStream(String eventStreamId, long fromVersion, long toVersion) {
        if (!flattenedStore.containsKey(checkNotNull(eventStreamId))) {
            return Optional.empty();
        }

        checkArgument(toVersion >= fromVersion,
                "toVersion (%s) must be greater than or equal to fromVersion (%s)", toVersion, fromVersion);
        checkArgument(fromVersion > 0,
                "fromVersion must be greater than zero, got %s", fromVersion);
        checkArgument(toVersion <= getMostRecentVersion(eventStreamId),
                "toVersion (%s) must be lesser than or equal to the most recent version", toVersion);

        List<DomainEvent> result = new ArrayList<>();
        for (long i = fromVersion - 1; i < toVersion; i++) {
            result.add(flattenedStore.get(eventStreamId).get((int) i).getEvent());
        }

        return Optional.of(EventStream.newFromVersion(fromVersion, result));
    }

    public long getMostRecentVersion(String eventStreamId) {
        if (!flattenedStore.containsKey(checkNotNull(eventStreamId))) {
            return 0L;
        }

        return flattenedStore.get(eventStreamId).size();
    }


    public synchronized String newStream(List<DomainEvent> events, Set<Metadata> metadata) {
        String aggregateId = UUID.randomUUID().toString();

        store.put(aggregateId, new ArrayList<>());
        flattenedStore.put(aggregateId, new ArrayList<>());

        List<StoredEvent> storedEvents = toStoredEvents(events, aggregateId);

        Map<String, Metadata> metadataMap = new HashMap<>();
        for (Metadata entry : metadata) {
            metadataMap.put(entry.getKey(), entry);
        }

        ChangeSet changeSet =
                new ChangeSet(UUID.randomUUID().toString(),
                        aggregateId,
                        storedEvents,
                        metadataMap);

        store.get(aggregateId).add(changeSet);
        flattenedStore.get(aggregateId).addAll(storedEvents);

        publish(changeSet);

        return aggregateId;
    }

    public synchronized void append(NewChangeSet command) {
        long currentVersion = getMostRecentVersion(command.getEventStreamId());

        if (currentVersion != command.getExpectedVersion()) {
            throw new IllegalStateException(
                    String.format("Expected version %s, got %s",
                            command.getExpectedVersion(), currentVersion));
        }

        List<StoredEvent> storedEvents =
                toStoredEvents(command.getEvents(), command.getEventStreamId());

        Map<String, Metadata> metadataMap = new HashMap<>();
        for (Metadata metadata : command.getMetadata()) {
            metadataMap.put(metadata.getKey(), metadata);
        }

        ChangeSet changeSet =
                new ChangeSet(UUID.randomUUID().toString(),
                        command.getEventStreamId(),
                        storedEvents,
                        metadataMap);

        store.get(command.getEventStreamId()).add(changeSet);
        flattenedStore.get(command.getEventStreamId()).addAll(storedEvents);

        publish(changeSet);
    }

    private List<StoredEvent> toStoredEvents(List<DomainEvent> events, String aggregateId) {
        List<StoredEvent> result = new ArrayList<>();

        for (DomainEvent event : events) {
            result.add(new StoredEvent(UUID.randomUUID().toString(), aggregateId, event));
        }

        return result;
    }

    private List<DomainEvent> fromStoredEvents(List<StoredEvent> storedEvents) {
        List<DomainEvent> result = new ArrayList<DomainEvent>();

        for (StoredEvent storedEvent : storedEvents) {
            result.add(storedEvent.getEvent());
        }

        return result;
    }

    public List<ChangeSet> getChangeSets(String eventStreamId) {
        return store.containsKey(eventStreamId)
                ? new ArrayList<>(store.get(eventStreamId))
                : Collections.<ChangeSet>emptyList();
    }

    public Optional<ChangeSet> getChangeSetById(String changeSetId) {
        for (List<ChangeSet> changeSets : store.values()){
            for (ChangeSet changeSet : changeSets) {
                if (changeSetId.equals(changeSet.getId())) {
                    return Optional.of(changeSet);
                }
            }
        }

        return Optional.empty();
    }
}
