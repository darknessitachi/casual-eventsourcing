package se.cs.eventsourcing.infrastructure.store.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;
import se.cs.eventsourcing.infrastructure.store.EventPublishingStore;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class MongoDBEventPublishingStore extends EventPublishingStore {
	private final MongoDBEventSourcingRepository repository;

	public MongoDBEventPublishingStore(MongoDatabase database, ObjectMapper objectMapper) {
		repository = new MongoDBEventSourcingRepository(database, objectMapper);
	}

	@Override public String newStream(List<DomainEvent> events, Set<Metadata> metadata) {
		checkNotNull(events, "No point in persisting an empty event stream.");
		checkArgument(!events.isEmpty(), "No point in persisting an empty event stream.");

		Document document = repository.insertStream(events.size());
		String streamId = document.getObjectId("_id").toHexString();

		String changesetid = repository.insertChangeSetAndMetadata(streamId, metadata);

		repository.insertEvents(streamId, changesetid, 0, events);

		return streamId;
	}

	@Override public void append(NewChangeSet command) {
		checkNotNull(command.getEvents(), "No point in persisting an empty event stream.");
		checkArgument(!command.getEvents().isEmpty(), "No point in persisting an empty event stream.");

		appendEvents(command, repository.insertChangeSetAndMetadata(command.getEventStreamId(), command.getMetadata()));
	}

	private synchronized void appendEvents(NewChangeSet command, String changeSetId) {
		long version = getMostRecentVersion(command.getEventStreamId());

		version = repository.insertEvents(command.getEventStreamId(), changeSetId, version, command.getEvents());

		repository.updateVersionOnStream(command.getEventStreamId(), version);
	}

	@Override public Optional<EventStream> loadStream(String eventStreamId, long fromVersion, long toVersion) {
		checkArgument(toVersion >= fromVersion,
				"toVersion (%s) must be greater than or equal to fromVersion (%s)", toVersion, fromVersion);
		checkArgument(fromVersion > 0,
				"fromVersion must be greater than zero, got %s", fromVersion);
		checkArgument(toVersion <= getMostRecentVersion(eventStreamId),
				"toVersion (%s) must be lesser than or equal to the most recent version", toVersion);

		List<DomainEvent> events = repository.loadEvents(eventStreamId, fromVersion, toVersion);
		return Optional.of(EventStream.newFromVersion(fromVersion, events));

	}

	@Override public long getMostRecentVersion(String eventStreamId) {
		return repository.getMostRecentStreamVersion(eventStreamId);
	}
}
