package se.cs.eventsourcing.domain.store.metadata;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Metadata {

    public static Metadatum withWhenNow() {
        return new Metadatum(KnownMetadata.WHEN.getKey(),
                ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
    }

    public static Metadatum withWhen(ZonedDateTime zonedDateTime) {
        return new Metadatum(KnownMetadata.WHEN.getKey(),
                checkNotNull(zonedDateTime).format(DateTimeFormatter.ISO_INSTANT));
    }

    public static Metadatum withUserReference(String userReference) {
        return new Metadatum(KnownMetadata.USER_REFERENCE.getKey(), checkNotNull(userReference));
    }

    public static Metadatum withMetadata(String key, String value) {
        checkNotNull(key);

        return new Metadatum(key, value);
    }
}
