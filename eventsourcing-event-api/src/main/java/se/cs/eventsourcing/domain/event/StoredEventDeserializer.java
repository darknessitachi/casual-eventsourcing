package se.cs.eventsourcing.domain.event;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;

import java.io.IOException;

public class StoredEventDeserializer extends JsonDeserializer<StoredEvent> {

    @Override
    public StoredEvent deserialize(JsonParser jsonParser,
                                   DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String id = node.get("id").asText();
        String eventStreamId = node.get("eventStreamId").asText();
        String canonicalName = node.get("canonicalName").asText();
        DomainEvent event = null;

        try {
            event =
                    (DomainEvent) node.get("event")
                            .traverse(jsonParser.getCodec())
                            .readValueAs((Class) Class.forName(canonicalName));

        } catch (ClassNotFoundException e) {
            Throwables.propagate(e);
        }

        return new StoredEvent(id, eventStreamId, event);
    }
}
