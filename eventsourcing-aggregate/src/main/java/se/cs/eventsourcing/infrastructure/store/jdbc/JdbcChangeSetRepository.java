package se.cs.eventsourcing.infrastructure.store.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import se.cs.eventsourcing.domain.changeset.ChangeSet;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.event.StoredEvent;
import se.cs.eventsourcing.domain.store.changeset.ChangeSetRepository;

import javax.sql.DataSource;
import java.util.*;

@Repository
public class JdbcChangeSetRepository implements ChangeSetRepository {

    private static String SELECT_CHANGESETS_BY_STREAMID = "select * from casual_changeset where stream_id = ? order by created_ asc";
    private static String SELECT_CHANGESET_BY_ID = "select * from casual_changeset where id = ?";

    private static String SELECT_EVENTS_BY_CHANGESET = "select id, stream_id, content_, class_ from casual_event where changeset_id = ?";
    private static String SELECT_METADATA_BY_CHANGESET = "select * from casual_metadata where changeset_id = ?";

    private JdbcTemplate template;
    private ObjectMapper mapper;

    @Autowired
    public JdbcChangeSetRepository(DataSource dataSource,
                                   ObjectMapper mapper) {

        this.template = new JdbcTemplate(dataSource);
        this.mapper = mapper;
    }

    @Override
    public List<ChangeSet> getChangeSets(String eventStreamId) {
        List<Map<String, Object>> rows =
                template.queryForList(SELECT_CHANGESETS_BY_STREAMID, eventStreamId);

        List<ChangeSet> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String id = (String) row.get("id");

            result.add(new ChangeSet(id,
                    eventStreamId,
                    getStoredEventsInChangeSet(id),
                    getMetadataInChangeSet(id)));
        }

        return result;
    }

    private List<StoredEvent> getStoredEventsInChangeSet(String id) {
        List<StoredEvent> storedEvents = new ArrayList<>();

        for (Map<String, Object> row : template.queryForList(SELECT_EVENTS_BY_CHANGESET, id)) {

            try {
                storedEvents.add(
                        new StoredEvent(
                                (String) row.get("id"),
                                (String) row.get("stream_id"),
                                mapper.readValue((String) row.get("content_"),
                                        (Class<DomainEvent>) Class.forName((String) row.get("class_")))));

            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }

        return storedEvents;
    }

    private Map<String, Metadata> getMetadataInChangeSet(String id) {
        Map<String, Metadata> metadata = new HashMap<>();

        for (Map<String, Object> row : template.queryForList(SELECT_METADATA_BY_CHANGESET, id)) {

            metadata.put((String) row.get("key_"),
                    new Metadata((String) row.get("key_"), (String) row.get("value_")));
        }

        return metadata;
    }

    @Override
    public Optional<ChangeSet> getChangeSetById(String changeSetId) {
        List<Map<String, Object>> rows =
                template.queryForList(SELECT_CHANGESET_BY_ID, changeSetId);

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> row = rows.get(0);

        return Optional.of(
                new ChangeSet(changeSetId,
                        (String) row.get("stream_id"),
                        getStoredEventsInChangeSet(changeSetId),
                        getMetadataInChangeSet(changeSetId)));
    }
}
