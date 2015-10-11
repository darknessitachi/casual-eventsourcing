package se.cs.eventsourcing.domain.changeset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChangeSetFactory {

    private ObjectMapper mapper;

    public ChangeSetFactory(ObjectMapper mapper) {
        this.mapper = checkNotNull(mapper);
    }

    public ChangeSet create(String serializedChangeSet) {
        try {
            return mapper.readValue(serializedChangeSet, ChangeSet.class);
        } catch (Exception e) {
            Throwables.propagate(e);
        }

        return null;
    }
}
