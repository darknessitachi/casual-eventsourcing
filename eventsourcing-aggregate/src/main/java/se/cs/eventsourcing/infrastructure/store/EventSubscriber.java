package se.cs.eventsourcing.infrastructure.store;

import se.cs.eventsourcing.domain.changeset.ChangeSet;

/**
 * If you want to receive events you better implement this
 * contract.
 */
public interface EventSubscriber {
    void handle(ChangeSet changeSet);
}
