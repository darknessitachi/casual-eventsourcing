package se.cs.eventsourcing.domain.aggregate;

import org.reflections.ReflectionUtils;
import se.cs.eventsourcing.domain.store.EventStream;
import se.cs.eventsourcing.domain.store.DomainEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class represents an aggregate root a la DDD. An aggregate
 * root can be conceived as a conceptual whole that form a transactional
 * consistency boundary.
 *
 * An aggregate root is an entity in DDD terms, identified via its
 * identity.
 */
public abstract class Aggregate {

    /**
     * The aggregate version, equal to the {@link EventStream}
     * version currently loaded.
     */
    private long version;

    /**
     * This list contain all state changes that make this aggregate dirty,
     * i.e. local changes that only exist in the present instance.
     */
    private List<DomainEvent> stateChanges = new ArrayList<>();

    /**
     * @return true if the instance is dirty
     */
    public boolean isDirty() {
        return !stateChanges.isEmpty();
    }

    /**
     * Returns a list of all state changes that make this aggregate dirty,
     * i.e. local changes that only exist in the present instance.
     */
    List<DomainEvent> changes() {
        return Collections.unmodifiableList(stateChanges);
    }

    /**
     * Resets the internal state back to where it was before local changes
     * were made, i.e. clears the internal event store log.
     */
    void clearChangesAndUpdateVersion() {
        version += stateChanges.size();
        stateChanges.clear();
    }

    /**
     * Adds an event to the list of changes.
     *
     * @param event the event to add
     */
    protected void append(DomainEvent event) {
        resolveDomainEventHandlerMethod(event, getDomainEventHandlerMethods());

        stateChanges.add(checkNotNull(event));
    }

    /**
     * Replays the stream of events, applying all events in sequence
     * to the current aggregate instance. For this to work all supported
     * event types must a corresponding event handler method - annotated
     * with {@link DomainEventHandler} - as the replay mechanism is
     * handled via reflection.
     *
     * @param stream the event stream to replay
     */
    public void replay(EventStream stream) {
        Set<Method> methods = getDomainEventHandlerMethods();

        for (DomainEvent event : stream.getEvents()) {
            invokeDomainEventHandlerMethod(event, methods);
        }

        this.version = stream.toVersion();
    }

    @SuppressWarnings("unchecked")
    private Set<Method> getDomainEventHandlerMethods() {
        return ReflectionUtils.getMethods(getClass(),
                ReflectionUtils.withAnnotation(DomainEventHandler.class),
                ReflectionUtils.withParametersCount(1));
    }

    private void invokeDomainEventHandlerMethod(DomainEvent event, Set<Method> methods) {
        Method method = resolveDomainEventHandlerMethod(event, methods);
        method.setAccessible(true);

        try {
            method.invoke(this, event);
        } catch (Exception e) {
            throw new DomainEventHandlerException(
                    String.format("Could not invoke domain eventstore handler method %s for store %s",
                            method, event));
        }

    }

    private Method resolveDomainEventHandlerMethod(DomainEvent event, Set<Method> methods) {
        for (Method method : methods) {
            Parameter first = method.getParameters()[0];

            if (first.getType().equals(event.getClass())){
                return method;
            }
        }

        throw new DomainEventHandlerException(
                String.format("No domain store handler method found for store %s",
                        event.getClass()));
    }

    /**
     * @return the version currently loaded
     */
    public long version() {
        return version;
    }

    public abstract String getEventStreamId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aggregate aggregate = (Aggregate) o;
        return Objects.equals(getEventStreamId(), aggregate.getEventStreamId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEventStreamId());
    }
}