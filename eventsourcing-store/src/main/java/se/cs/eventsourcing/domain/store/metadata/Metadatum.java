package se.cs.eventsourcing.domain.store.metadata;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class Metadatum {

    private final String key;
    private final String value;

    Metadatum(String key, String value) {
        this.key = checkNotNull(key);
        this.value = checkNotNull(value);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadatum metadatum = (Metadatum) o;
        return Objects.equals(key, metadatum.key) &&
                Objects.equals(value, metadatum.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
