package se.cs.eventsourcing.domain.aggregate;

/**
 * Denotes that something went wrong during usage of
 * methods annotated with the {@link DomainEventHandler}
 * annotation.
 */
public class DomainEventHandlerException extends RuntimeException {

    public DomainEventHandlerException(String message) {
        super(message);
    }

    public DomainEventHandlerException(Throwable t) {
        super(t);
    }
}
