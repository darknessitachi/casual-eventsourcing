package se.cs.eventsourcing.infrastructure.store.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.cs.eventsourcing.domain.changeset.ChangeSet;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MongoDBEventPublishingStoreTest {

	private MongoDatabase database;
	private MongoDBEventPublishingStore store;
	private ObjectMapper objectMapper;

	public MongoDBEventPublishingStoreTest() {
		objectMapper = new ObjectMapper();
	}

	@Before public void setUp() throws Exception {
		MongodForTestsFactory factory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
		MongoClient mongoClient = factory.newMongo();
		database = mongoClient.getDatabase("test");

		store = new MongoDBEventPublishingStore(database, objectMapper);
	}

	@After public void tearDown() throws Exception {
		database.drop();
	}

	@Test public void testNewStream() throws Exception {
		String streamId = store.newStream(createEvents(1, 8), createMetadata(8));
		Optional<EventStream> eventStream = store.loadStream(streamId, 1, 5);

		assertTrue(eventStream.isPresent());
		EventStream stream = eventStream.get();
		assertEquals(1, stream.fromVersion());
		assertEquals(5, stream.getEvents().size());
	}

	@Test public void testNewStream_Metadata() throws Exception {
		String streamId = store.newStream(createEvents(1, 8), createMetadata(8));

		MongoDBChangeSetRepository repository = new MongoDBChangeSetRepository(database, objectMapper);
		List<ChangeSet> changeSets =
				repository.getChangeSets(streamId);
		assertEquals(8, changeSets.get(0).getMetadata().size());
	}

	@Test public void testAppend() throws Exception {
		String streamId = store.newStream(createEvents(1, 3), createMetadata(5));

		store.append(new NewChangeSet(streamId, 2, createEvents(4, 5), new HashSet<>()));

		Optional<EventStream> eventStream = store.loadStream(streamId, 2, 6);
		assertTrue(eventStream.isPresent());
		EventStream stream = eventStream.get();
		assertEquals(2, stream.fromVersion());
		assertEquals(5, stream.getEvents().size());
	}

	@Test public void testAppend_TwoStreams() throws Exception {
		String streamId1 = store.newStream(createEvents(1, 3), createMetadata(5));
		String streamId2 = store.newStream(createEvents(1, 3), createMetadata(5));

		store.append(new NewChangeSet(streamId1, 2, createEvents(4, 5), new HashSet<>()));
		store.append(new NewChangeSet(streamId2, 2, createEvents(4, 5), new HashSet<>()));

		Optional<EventStream> eventStream = store.loadStream(streamId1, 2, 6);
		assertTrue(eventStream.isPresent());
		EventStream stream = eventStream.get();
		assertEquals(2, stream.fromVersion());
		assertEquals(5, stream.getEvents().size());

		eventStream = store.loadStream(streamId2, 2, 6);
		assertTrue(eventStream.isPresent());
		stream = eventStream.get();
		assertEquals(2, stream.fromVersion());
		assertEquals(5, stream.getEvents().size());
	}

	@Test public void testGetMostRecentVersion_WithAppend() throws Exception {
		String streamId = store.newStream(createEvents(1,1), createMetadata(3));
		store.append(new NewChangeSet(streamId, 2, createEvents(2, 7), new HashSet<>()));

		long mostRecentVersion = store.getMostRecentVersion(streamId);

		assertEquals(8, mostRecentVersion);
	}
	@Test public void testGetMostRecentVersion() throws Exception {
		String streamId = store.newStream(createEvents(1, 5), createMetadata(7));

		long mostRecentVersion = store.getMostRecentVersion(streamId);

		assertEquals(5, mostRecentVersion);
	}

	@Test public void testGetMostRecentVersion_TwoStreams() throws Exception {
		String streamId1 = store.newStream(createEvents(1, 5), createMetadata(7));
		String streamId2 = store.newStream(createEvents(1, 3), createMetadata(7));

		long mostRecentVersion1 = store.getMostRecentVersion(streamId1);
		long mostRecentVersion2 = store.getMostRecentVersion(streamId2);

		assertEquals(5, mostRecentVersion1);
		assertEquals(3, mostRecentVersion2);
	}

	private List<DomainEvent> createEvents(int fromVersion, int count) {
		return IntStream.range(fromVersion, fromVersion + count)
				.mapToObj(i -> new ChangeFirstName("kalle" + i))
				.collect(Collectors.toList());
	}

	private Set<Metadata> createMetadata(int count) {
		return IntStream.range(0, count)
				.mapToObj(i -> new Metadata("hej" + i, "hopp" + i))
				.collect(Collectors.toSet());
	}
}