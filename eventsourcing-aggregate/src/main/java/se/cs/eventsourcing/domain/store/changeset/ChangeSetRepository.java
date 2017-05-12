package se.cs.eventsourcing.domain.store.changeset;

import se.cs.eventsourcing.domain.changeset.ChangeSet;

import java.util.List;
import java.util.Optional;

/**
 * A read only repository that can be used to
 * query the event store.
 */
public interface ChangeSetRepository {

    List<ChangeSet> getChangeSets(String eventStreamId);

    Optional<ChangeSet> getChangeSetById(String changeSetId);
}
