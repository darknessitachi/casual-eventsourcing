package se.cs.eventsourcing.domain.aggregate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate domain event handler methods in
 * aggregate classes to facilitate replay functionality
 * via reflection.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainEventHandler {}
