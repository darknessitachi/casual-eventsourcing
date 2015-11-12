package se.cs.eventsourcing.infrastructure.store.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import se.cs.eventsourcing.domain.changeset.ChangeSet;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.event.StoredEvent;
import se.cs.eventsourcing.domain.store.changeset.ChangeSetRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

/**
 * A repository reading change sets from a MongoDB.
 *
 * The repository contains three collections with the following schemas:
 * - ChangeSets
 *   {
 *       id: "123",
 *       stream_id: "aaa",
 *   }
 * - CasualEvents
 *   {
 *       changeset_id: "123",
 *       id: "4433",
 *       eventStreamId: "aStream",
 *       class_: "se.cs.eventsourcing.infrastructure.store.mongodb.ChangeFirstName",
 *       content_:"[serialized representation of an instance of class_]"
 *   }
 * - CasualMetadata
 *   {
 *       changeset_id: "123",
 *       metadata: [
 *       	{
 *       	  key: "name",
 *       	  value: "aRandomName"
 *       	}
 *       ]
 *   }
 */
public class MongoDBChangeSetRepository implements ChangeSetRepository {

	public static final String CHANGE_SETS_COLLECTION = "ChangeSets";
	public static final String CASUAL_EVENTS_COLLECTION = "CasualEvents";
	public static final String CASUAL_METADATA_COLLECTION = "CasualMetadata";
	private final MongoDatabase database;
	private final ObjectMapper objectMapper;

	public MongoDBChangeSetRepository(MongoDatabase database, ObjectMapper objectMapper) {
		this.database = database;
		this.objectMapper = objectMapper;
	}

	@Override public List<ChangeSet> getChangeSets(String eventStreamId) {
		MongoCollection<Document> changeSets = database.getCollection(CHANGE_SETS_COLLECTION);
		FindIterable<Document> iterable = changeSets.find(eq("stream_id", eventStreamId));
		return MongoDBUtils.getStream(iterable)
				.map(this::convertToChangeSet)
				.collect(Collectors.toList());
	}

	private ChangeSet convertToChangeSet(Document document) {
		String changeSetId = document.getObjectId("_id").toHexString();
		return new ChangeSet(changeSetId,
				document.getString("stream_id"),
				getStoredEventIdsInChangeSet(changeSetId),
				getMetadataInChangeSet(changeSetId)
		);
	}

	@Override public Optional<ChangeSet> getChangeSetById(String changeSetId) {
		MongoCollection<Document> changeSets = database.getCollection(CHANGE_SETS_COLLECTION);
		FindIterable<Document> iterable = changeSets.find(eq("_id", new ObjectId(changeSetId)));

		return Optional.ofNullable(iterable.first()).map(d ->
			new ChangeSet(changeSetId,
					d.getString("stream_id"),
					getStoredEventIdsInChangeSet(changeSetId),
					getMetadataInChangeSet(changeSetId)
			)
		);
	}

	private Map<String, Metadata> getMetadataInChangeSet(String changeSetId) {
		MongoCollection<Document> casualMetadata = database.getCollection(CASUAL_METADATA_COLLECTION);
		FindIterable<Document> events = casualMetadata.find(eq("changeset_id", changeSetId));
		return Optional.ofNullable(events.first())
				.map(d -> (List<Document>) d.get("metadata"))
				.orElse(new ArrayList<>())
				.stream()
				.collect(Collectors
						.toMap(d -> d.getString("key"),
								d -> new Metadata(d.getString("key"), d.getString("value")
								)
						)
				);
	}

	private List<StoredEvent> getStoredEventIdsInChangeSet(String changeSetId) {
		MongoCollection<Document> casualEvents = database.getCollection(CASUAL_EVENTS_COLLECTION);
		FindIterable<Document> events = casualEvents.find(eq("changeset_id", changeSetId));
			return MongoDBUtils.getStream(events)
					.map(this::convertToStoredEvent)
					.collect(Collectors.toList());
	}

	private StoredEvent convertToStoredEvent(Document document) {
		try {
			return new StoredEvent(document.getObjectId("_id").toHexString(),
					document.getString("eventStreamId"),
					objectMapper.readValue(document.getString("content_"),
							(Class<DomainEvent>) Class.forName(document.getString("class_"))
					)
			);
		}
		catch (IOException | ClassNotFoundException e) {
			Throwables.propagate(e);
		}
		return null;
	}
}
