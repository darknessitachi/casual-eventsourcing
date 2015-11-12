package se.cs.eventsourcing.infrastructure.store.mongodb;

import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MongoDBUtils {
	public static Stream<Document> getStream(FindIterable<Document> iterable) {
		return StreamSupport
				.stream(
						Spliterators.spliteratorUnknownSize(
								iterable.iterator(),
								Spliterator.IMMUTABLE
						),
						false
				);
	}
}
