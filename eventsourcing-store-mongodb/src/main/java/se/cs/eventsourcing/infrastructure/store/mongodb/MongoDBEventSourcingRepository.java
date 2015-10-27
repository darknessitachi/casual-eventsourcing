package se.cs.eventsourcing.infrastructure.store.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MongoDBEventSourcingRepository {
	public static final String CASUAL_STREAMS_COLLECTION = "CasualStreams";
	private final MongoDatabase database;
	private final ObjectMapper objectMapper;

	public MongoDBEventSourcingRepository(MongoDatabase database, ObjectMapper objectMapper) {
		this.database = database;
		this.objectMapper = objectMapper;
	}

	/**
	 * Inserts a stream.
	 * @param version the version
	 * @return a document representing the stream
	 */
	public Document insertStream(int version) {
		MongoCollection<Document> casualStreams = database.getCollection(CASUAL_STREAMS_COLLECTION);
		Document document = new Document("version_", (long) version);
		casualStreams.insertOne(document);
		return document;
	}

	/**
	 * Inserts events
	 * @param eventStreamId
	 * @param changeSetId
	 * @param version
	 * @param events
	 * @return
	 */
	public long insertEvents(String eventStreamId, String changeSetId, long version, List<DomainEvent> events) {
		MongoCollection<Document> collection = database
				.getCollection(MongoDBChangeSetRepository.CASUAL_EVENTS_COLLECTION);
		final AtomicLong atomicVersion = new AtomicLong(version);
		List<Document> newEvents = events
				.stream()
				.map(event -> createEventDocument(changeSetId, atomicVersion, event, eventStreamId))
				.collect(Collectors.toList());
		collection.insertMany(newEvents);
		return atomicVersion.get();
	}

	private Document createEventDocument(String changeSetId, AtomicLong version, DomainEvent event, String streamId) {
		try {
			return new Document("changeset_id", changeSetId)
					.append("version_", version.incrementAndGet())
					.append("eventStreamId", streamId)
					.append("class_", event.getClass().getCanonicalName())
					.append("content_", objectMapper.writeValueAsString(event));
		}
		catch (JsonProcessingException e) {
			Throwables.propagate(e);
		}
		return null;
	}

	/**
	 * Loads events between two versions
	 *
	 * @param eventStreamId
	 * @param fromVersion lower version
	 * @param toVersion upper version
	 * @return the events
	 */
	public List<DomainEvent> loadEvents(String eventStreamId, long fromVersion, long toVersion) {
		MongoCollection<Document> collection = database
				.getCollection(MongoDBChangeSetRepository.CASUAL_EVENTS_COLLECTION);

		FindIterable<Document> documents = collection
				.find(new Document("version_",
						new Document("$gte", fromVersion)
								.append("$lte", toVersion)
					).append("eventStreamId", eventStreamId)
				);
		return MongoDBUtils.getStream(documents)
				.map(this::createDomainEvent)
				.collect(Collectors.toList());
	}

	private DomainEvent createDomainEvent(Document d) {
		try {
			return objectMapper
					.readValue(d.getString("content_"), (Class<DomainEvent>) Class.forName(d.getString("class_")));
		}
		catch (IOException | ClassNotFoundException e) {
			Throwables.propagate(e);
		}
		return null;
	}

	/**
	 * Fetches the most recent version of a stream.
	 * @return the most recent version.
	 * @param eventStreamId
	 */
	public long getMostRecentStreamVersion(String eventStreamId) {
		MongoCollection<Document> casualStreams = database.getCollection(CASUAL_STREAMS_COLLECTION);
		AggregateIterable<Document> aggregate = casualStreams.aggregate(
				Lists.newArrayList(
						new Document("$match",
								new Document("_id", new ObjectId(eventStreamId))
						),
						new Document("$group",
								new Document("_id", "$_id")
										.append("maxVersion", new Document("$max", "$version_"))
						)
				)
		);
		return Optional
				.ofNullable(aggregate.first())
				.map(d -> d.getLong("maxVersion"))
				.orElse(0L);
	}

	/**
	 * Inserts metadata for a stream.
	 * @param streamId streamId
	 * @param metadata metadata for stream
	 * @return
	 */
	String insertChangeSetAndMetadata(String streamId, Set<Metadata> metadata) {
		Document document = insertChangeSet(streamId);

		String id = document.getObjectId("_id").toHexString();

		insertMetadata(metadata, id);
		return id;
	}

	private Document insertChangeSet(String streamId) {
		MongoCollection<Document> collection = database
				.getCollection(MongoDBChangeSetRepository.CHANGE_SETS_COLLECTION);
		Document document = createChangeSetDocument(streamId);
		collection.insertOne(document);
		return document;
	}

	private Document createChangeSetDocument(String streamId) {
		return new Document("stream_id", streamId)
				.append("created", new Timestamp(new Date().getTime()).toInstant().toEpochMilli());
	}

	private void insertMetadata(Set<Metadata> metadata, String id) {
		MongoCollection<Document> mdCollection = database
				.getCollection(MongoDBChangeSetRepository.CASUAL_METADATA_COLLECTION);
		mdCollection.insertOne(createMetadataDocument(metadata, id));
	}

	/**
	 * Updates the version on a streams object.
	 * @param eventStreamId streamId
	 * @param value new version
	 */
	void updateVersionOnStream(String eventStreamId, long value) {
		MongoCollection<Document> streamCollection = database.getCollection(CASUAL_STREAMS_COLLECTION);
		streamCollection.updateOne(
				new Document("_id", new ObjectId(eventStreamId)),
				new Document("$set", new Document("version_", value))
		);
	}

	private Document createMetadataDocument(Set<Metadata> metadata, String id) {
		return new Document("changeset_id", id)
				.append("metadata",
						metadata.stream()
								.map(md -> new Document("key", md.getKey()).append("value", md.getValue()))
								.collect(Collectors.toList()));
	}
}
