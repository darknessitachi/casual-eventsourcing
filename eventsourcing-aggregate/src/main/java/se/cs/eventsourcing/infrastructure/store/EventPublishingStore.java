package se.cs.eventsourcing.infrastructure.store;

import se.cs.eventsourcing.domain.changeset.ChangeSet;
import se.cs.eventsourcing.domain.store.EventStore;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class that event stores can extend to get some
 * basic publish/subscribe functionality.
 */
public abstract class EventPublishingStore implements EventStore {

    private List<EventSubscriber> subscribers = new ArrayList<>();

    /**
     * Subscribes the object to the list of subscribers.
     * @param subscriber a subscriber
     */
    public void subscribe(EventSubscriber subscriber) {
        this.subscribers.add(checkNotNull(subscriber));
    }

    public void setSubscribers(List<EventSubscriber> subscribers) {
        this.subscribers = checkNotNull(subscribers);
    }

    /**
     * Publishes the changeset to all registered subscribers.
     * @param changeSet the changeset to publish
     */
    protected void publish(ChangeSet changeSet) {
        subscribers.forEach(
                subscriber -> subscriber.handle(changeSet));
    }
}
