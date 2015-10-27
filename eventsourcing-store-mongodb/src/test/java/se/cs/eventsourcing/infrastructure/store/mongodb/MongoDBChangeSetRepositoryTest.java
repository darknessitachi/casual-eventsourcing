package se.cs.eventsourcing.infrastructure.store.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.cs.eventsourcing.domain.changeset.ChangeSet;
import se.cs.eventsourcing.domain.event.StoredEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.cs.eventsourcing.infrastructure.store.mongodb.MongoDBChangeSetRepository.*;

public class MongoDBChangeSetRepositoryTest {

	private MongoDatabase database;
	private ObjectMapper mapper;
	private String streamid = "testid";

	@Before
	public void before() throws IOException {
		MongodForTestsFactory factory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
		MongoClient mongoClient = factory.newMongo();
		database = mongoClient.getDatabase("test");
		mapper = new ObjectMapper();
	}

	@After
	public void after() {
		database.drop();
	}

	@Test
	public void testGetChangeSetById() throws IOException {
		String changesetid = createTestData();

		MongoDBChangeSetRepository repository = new MongoDBChangeSetRepository(database, new ObjectMapper());
		Optional<ChangeSet> testid = repository.getChangeSetById(changesetid);

		assertTrue(testid.isPresent());
		assertEquals(changesetid, testid.get().getId());
		assertEquals("testid", testid.get().getEventStreamId());
		List<StoredEvent> storedEvents = testid.get().getStoredEvents();
		assertEquals(1, storedEvents.size());
		assertEquals("sune", storedEvents.get(0).getEventStreamId());
		assertEquals("olle", ((ChangeFirstName)storedEvents.get(0).getEvent()).getName());
		assertEquals("aRandomName", testid.get().getMetadata().get("name").getValue());
	}

	@Test
	public void testGetChangeSets() throws IOException {
		String changesetid = createTestData();

		MongoDBChangeSetRepository repository = new MongoDBChangeSetRepository(database, new ObjectMapper());
		List<ChangeSet> result = repository.getChangeSets(streamid);

		assertEquals(1, result.size());
		ChangeSet changeSet = result.get(0);
		assertEquals(changesetid, changeSet.getId());
		assertEquals("testid", changeSet.getEventStreamId());
		List<StoredEvent> storedEvents = changeSet.getStoredEvents();
		assertEquals(1, storedEvents.size());
		assertEquals("sune", storedEvents.get(0).getEventStreamId());
		assertEquals("olle", ((ChangeFirstName)storedEvents.get(0).getEvent()).getName());
		assertEquals("aRandomName", changeSet.getMetadata().get("name").getValue());
	}

	private String createTestData() throws JsonProcessingException {
		database.createCollection(CHANGE_SETS_COLLECTION);
		MongoCollection<Document> changeSets = database.getCollection(CHANGE_SETS_COLLECTION);
		Document document = new Document("stream_id", streamid);
		changeSets.insertOne(document);
		String changesetid = document.getObjectId("_id").toHexString();

		database.createCollection(CASUAL_EVENTS_COLLECTION);
		MongoCollection<Document> events = database.getCollection(CASUAL_EVENTS_COLLECTION);
		Document newEvent = new Document("changeset_id", changesetid)
				.append("eventStreamId", "sune")
				.append("class_", ChangeFirstName.class.getCanonicalName())
				.append("content_", mapper.writeValueAsString(new ChangeFirstName("olle")));
		events.insertOne(newEvent);

		database.createCollection(CASUAL_METADATA_COLLECTION);
		MongoCollection<Document> metadata = database
				.getCollection(CASUAL_METADATA_COLLECTION);
		Document newMetadata = new Document("changeset_id", changesetid).append("metadata",
				Arrays.asList(new Document("key", "name").append("value", "aRandomName")));
		metadata.insertOne(newMetadata);

		return changesetid;
	}
}