package se.cs.eventsourcing.infrastructure.store;

import se.cs.eventsourcing.domain.store.event.DomainEvent;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.changeset.ChangeSet;
import se.cs.eventsourcing.domain.store.changeset.ChangeSetRepository;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;
import se.cs.eventsourcing.domain.store.changeset.StoredEvent;
import se.cs.eventsourcing.domain.store.metadata.Metadatum;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A basic in memory event store, just for fun!
 */
public class InMemoryEventStore extends EventPublishingStore implements ChangeSetRepository {

    private final Map<String, List<ChangeSet>> store =new HashMap<>();

    private final Map<String, List<StoredEvent>> flattenedStore = new HashMap<>();

    public Optional<EventStream> loadStream(String eventStreamId) {
        if (!flattenedStore.containsKey(checkNotNull(eventStreamId))) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                EventStream.newFromStart(
                        fromStoredEvents(flattenedStore.get(eventStreamId))));
    }

    public long getMostRecentVersion(String eventStreamId) {
        Optional<EventStream> stream = loadStream(eventStreamId);

        return stream.isPresent()
                ? stream.get().toVersion()
                : 0L;
    }


    public synchronized String newStream(List<DomainEvent> events, Set<Metadatum> metadata) {
        String aggregateId = UUID.randomUUID().toString();

        store.put(aggregateId, new ArrayList<>());
        flattenedStore.put(aggregateId, new ArrayList<>());

        List<StoredEvent> storedEvents = toStoredEvents(events, aggregateId, 1);

        Map<String, Metadatum> metadataMap = new HashMap<>();
        for (Metadatum metadatum : metadata) {
            metadataMap.put(metadatum.getKey(), metadatum);
        }

        ChangeSet changeSet =
                new ChangeSet(1,
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
                toStoredEvents(command.getEvents(), command.getEventStreamId(), currentVersion + 1);

        Map<String, Metadatum> metadataMap = new HashMap<>();
        for (Metadatum metadatum : command.getMetadata()) {
            metadataMap.put(metadatum.getKey(), metadatum);
        }

        ChangeSet changeSet =
                new ChangeSet(store.get(command.getEventStreamId()).size() + 1,
                        command.getEventStreamId(),
                        storedEvents,
                        metadataMap);

        store.get(command.getEventStreamId()).add(changeSet);
        flattenedStore.get(command.getEventStreamId()).addAll(storedEvents);

        publish(changeSet);
    }

    private List<StoredEvent> toStoredEvents(List<DomainEvent> events, String aggregateId, long startId) {
        List<StoredEvent> result = new ArrayList<>();

        for (DomainEvent event : events) {
            result.add(new StoredEvent(startId++, aggregateId, event));
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

    public Optional<ChangeSet> getChangeSetById(String eventStreamId, long changeSetId) {
        if (!store.containsKey(eventStreamId)) {
            return Optional.empty();
        }

        for (ChangeSet changeSet : store.get(eventStreamId)) {
            if (changeSet.getId() == changeSetId) {
                return Optional.of(changeSet);
            }
        }

        return Optional.empty();
    }

    public Optional<ChangeSet> getChangeSetByEventId(String eventStreamId, long eventId) {
        if (!store.containsKey(eventStreamId)) {
            return Optional.empty();
        }

        for (ChangeSet changeSet : store.get(eventStreamId)) {
            if (changeSet.containsEvent(eventId)) {
                return Optional.of(changeSet);
            }
        }

        return Optional.empty();
    }
}
