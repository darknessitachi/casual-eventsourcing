package se.cs.eventsourcing.domain.changeset;

public enum KnownMetadata {
    USER_REFERENCE("casual.eventsourcing.metadata.userreference"),
    WHEN("casual.eventsourcing.metadata.when");

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
