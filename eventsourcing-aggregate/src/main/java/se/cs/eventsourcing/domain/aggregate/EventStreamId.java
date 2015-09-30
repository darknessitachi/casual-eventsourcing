package se.cs.eventsourcing.domain.aggregate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark which field in
 * the {@link Aggregate} subclass that should carry
 * the event stream id. The field visibility should
 * be a private String, and must not under any
 * circumstances be set; the {@link AggregateRepository}
 * will take care of that when the aggregate is saved for the
 * first time.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventStreamId {}
