package se.cs.eventsourcing.domain.changeset;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class Metadata {

    private String key;
    private String value;

    private Metadata() {}

    public Metadata(String key, String value) {
        this.key = checkNotNull(key);
        this.value = checkNotNull(value);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static Metadata withWhen() {
        return new Metadata(KnownMetadata.WHEN.getKey(),
                ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
    }

    public static Metadata withWhen(ZonedDateTime zonedDateTime) {
        return new Metadata(KnownMetadata.WHEN.getKey(),
                checkNotNull(zonedDateTime).format(DateTimeFormatter.ISO_INSTANT));
    }

    public static Metadata withUserReference(String userReference) {
        return new Metadata(KnownMetadata.USER_REFERENCE.getKey(), checkNotNull(userReference));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(key, metadata.key) &&
                Objects.equals(value, metadata.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
