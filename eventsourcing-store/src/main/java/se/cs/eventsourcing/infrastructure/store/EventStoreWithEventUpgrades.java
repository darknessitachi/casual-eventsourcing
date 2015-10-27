package se.cs.eventsourcing.infrastructure.store;

import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.EventStore;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;
import se.cs.eventsourcing.domain.store.event.EventUpgradeService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple decorator that utilize the {@link EventUpgradeService}
 * to make sure that events are always upgraded accordingly whenever
 * streams are loaded.
 */
public class EventStoreWithEventUpgrades implements EventStore {

    private final EventStore eventStore;
    private final EventUpgradeService upgradeService;

    public EventStoreWithEventUpgrades(EventStore eventStore,
                                       EventUpgradeService upgradeService) {

        this.eventStore = checkNotNull(eventStore);
        this.upgradeService = checkNotNull(upgradeService);
    }

    @Override
    public String newStream(List<DomainEvent> events, Set<Metadata> metadata) {
        return eventStore.newStream(events, metadata);
    }

    @Override
    public void append(NewChangeSet command) {
        eventStore.append(command);
    }

    @Override
    public Optional<EventStream> loadStream(String eventStreamId, long fromVersion, long toVersion) {

        Optional<EventStream> stream = eventStore.loadStream(eventStreamId, fromVersion, toVersion);

        if (!stream.isPresent()) {
            return Optional.empty();
        }

        List<DomainEvent> upgraded = stream.get().getEvents()
                .stream()
                .map(upgradeService::upgrade)
                .collect(Collectors.toList());

        return Optional.of(
                EventStream.newFromVersion(stream.get().fromVersion(), upgraded));
    }

    @Override
    public long getMostRecentVersion(String eventStreamId) {
        return eventStore.getMostRecentVersion(eventStreamId);
    }
}
