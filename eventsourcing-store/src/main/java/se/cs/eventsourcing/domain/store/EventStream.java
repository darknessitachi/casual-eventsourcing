package se.cs.eventsourcing.domain.store;

import se.cs.eventsourcing.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A stream of events!
 */
public class EventStream {

    /**
     * The sequence of events in the stream
     */
    private final List<DomainEvent> events;

    private final VersionRange versionRange;

    private EventStream(VersionRange versionRange, List<DomainEvent> events) {
        this.versionRange = checkNotNull(versionRange);
        this.events = new ArrayList<>(checkNotNull(events));
    }

    /**
     * Creates a new event stream from the first version (i.e. 1) up
     * to the version equal to the number of events supplied via the
     * method parameter.
     *
     * @param events the list of events - in proper sequence - from
     *               the dawn of time for the aggregate in question
     * @return the event stream
     */
    public static EventStream newFromStart(List<DomainEvent> events) {
        return new EventStream(new VersionRange(1, events.size()), events);
    }

    /**
     * Creates a new event stream from the specified version up to
     * the version equal to fromVersion + events.size(). The idea
     * behind this factory method is to support snapshots further
     * down the road.
     *
     * @param fromVersion the version of the first event in the list
     * @param events the list of events - in proper sequence - since
     *               fromVersion up to the desired toVersion
     *
     * @return the event stream
     */
    public static EventStream newFromVersion(long fromVersion, List<DomainEvent> events) {
        return new EventStream(new VersionRange(fromVersion, fromVersion + events.size() - 1), events);
    }

    public long fromVersion() {
        return versionRange.getFrom();
    }

    public long toVersion() {
        return versionRange.getTo();
    }

    public List<DomainEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public static class VersionRange {
        private long from;
        private long to;

        VersionRange(long from, long to) {
            checkArgument(from > 0);
            checkArgument(to > 0);
            checkArgument(from <= to);

            this.from = from;
            this.to = to;
        }

        VersionRange(long to) {
            this(1, to);
        }

        public long getFrom() {
            return from;
        }

        public long getTo() {
            return to;
        }
    }
}
