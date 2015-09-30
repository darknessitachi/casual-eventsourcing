package se.cs.eventsourcing.domain.store.metadata;

public enum KnownMetadata {
    USER_REFERENCE("casual.store.metadatum.user"),
    WHEN("casual.store.metadatum.when");

    private String key;

    KnownMetadata(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static boolean containsKey(String key) {
        for (KnownMetadata metadata : values()) {
            if (metadata.getKey().equals(key)) {
                return true;
            }
        }

        return false;
    }
}
