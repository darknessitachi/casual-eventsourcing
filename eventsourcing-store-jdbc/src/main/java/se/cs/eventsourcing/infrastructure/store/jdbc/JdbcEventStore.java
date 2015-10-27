package se.cs.eventsourcing.infrastructure.store.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.cs.eventsourcing.domain.changeset.Metadata;
import se.cs.eventsourcing.domain.event.DomainEvent;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.changeset.NewChangeSet;
import se.cs.eventsourcing.infrastructure.store.EventPublishingStore;

import javax.sql.DataSource;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Repository
public class JdbcEventStore extends EventPublishingStore {

    private static String INSERT_CHANGESET = "insert into casual_changeset (id, stream_id, created_) values (?, ?, ?)";
    private static String INSERT_METADATA = "insert into casual_metadata (id, key_, value_, changeset_id) values (?, ?, ?, ?)";
    private static String INSERT_STREAM = "insert into casual_stream (id, version_) values (?, ?)";
    private static String INSERT_EVENT = "insert into casual_event (id, stream_id, class_, content_, version_, changeset_id) values (?, ?, ?, ?, ?, ?)";

    private static String UPDATE_STREAM_VERSION = "update casual_stream set version_ = ? where id = ?";

    private static String SELECT_EVENTSTREAM = "select class_, content_ from casual_event where stream_id = ? and version_ >= ? and version_ <= ? order by version_ asc";
    private static String SELECT_EVENTSTREAM_VERSION = "select version_ from casual_stream where id = ?";

    private JdbcTemplate template;
    private ObjectMapper mapper;

    @Autowired
    public JdbcEventStore(DataSource dataSource,
                          ObjectMapper mapper) {

        this.template = new JdbcTemplate(dataSource);
        this.mapper = mapper;
    }

    @Override
    @Transactional("txManager")
    public String newStream(List<DomainEvent> events,
                            Set<Metadata> metadata) {

        checkNotNull(events, "No point in persisting an empty event stream.");
        checkArgument(!events.isEmpty(), "No point in persisting an empty event stream.");

        String streamId = UUID.randomUUID().toString();
        template.update(INSERT_STREAM,
                streamId,
                events.size());

        String changeSetId = insertChangeSetAndMetadata(streamId, metadata);

        long version = 1;
        try {
            for (DomainEvent event : events) {
                template.update(INSERT_EVENT,
                        UUID.randomUUID().toString(),
                        streamId,
                        event.getClass().getCanonicalName(),
                        mapper.writeValueAsString(event),
                        version++,
                        changeSetId);
            }
        } catch (JsonProcessingException e) {
            Throwables.propagate(e);
        }

        return streamId;
    }

    @Override
    @Transactional("txManager")
    public void append(NewChangeSet command) {
        checkNotNull(command.getEvents(), "No point in persisting an empty event stream.");
        checkArgument(!command.getEvents().isEmpty(), "No point in persisting an empty event stream.");

        appendEvents(command, insertChangeSetAndMetadata(command.getEventStreamId(), command.getMetadata()));
    }

    private String insertChangeSetAndMetadata(String eventStreamId, Set<Metadata> metadata) {
        String changesetId = UUID.randomUUID().toString();
        template.update(INSERT_CHANGESET,
                changesetId,
                eventStreamId,
                new java.sql.Timestamp(new Date().getTime()));

        for (Metadata entry : metadata) {
            template.update(INSERT_METADATA,
                    UUID.randomUUID().toString(),
                    entry.getKey(),
                    entry.getValue(),
                    changesetId);
        }

        return changesetId;
    }

    private synchronized void appendEvents(NewChangeSet command, String changeSetId) {
        long version = getMostRecentVersion(command.getEventStreamId());

        for (DomainEvent event : command.getEvents()) {
            try {
                template.update(INSERT_EVENT,
                        UUID.randomUUID().toString(),
                        command.getEventStreamId(),
                        event.getClass().getCanonicalName(),
                        mapper.writeValueAsString(event),
                        ++version,
                        changeSetId);

            } catch (JsonProcessingException e) {
                Throwables.propagate(e);
            }
        }

        template.update(UPDATE_STREAM_VERSION, version, command.getEventStreamId());
    }

    @Override
    public Optional<EventStream> loadStream(String eventStreamId, long fromVersion, long toVersion) {

        checkArgument(toVersion >= fromVersion,
                "toVersion (%s) must be greater than or equal to fromVersion (%s)", toVersion, fromVersion);
        checkArgument(fromVersion > 0,
                "fromVersion must be greater than zero, got %s", fromVersion);
        checkArgument(toVersion <= getMostRecentVersion(eventStreamId),
                "toVersion (%s) must be lesser than or equal to the most recent version", toVersion);

        List<Map<String, Object>> rows =
                template.queryForList(SELECT_EVENTSTREAM, eventStreamId, fromVersion, toVersion);

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        List<DomainEvent> events = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            try {
                events.add(mapper.readValue((String) row.get("content_"),
                        (Class<DomainEvent>) Class.forName((String) row.get("class_"))));

            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }

        return Optional.of(EventStream.newFromVersion(fromVersion, events));
    }

    @Override
    public long getMostRecentVersion(String eventStreamId) {
        List<Map<String, Object>> rows =
                template.queryForList(SELECT_EVENTSTREAM_VERSION, eventStreamId);

        if (rows.isEmpty()) {
            return 0;
        }

        return (long) rows.get(0).get("version_");
    }
}
