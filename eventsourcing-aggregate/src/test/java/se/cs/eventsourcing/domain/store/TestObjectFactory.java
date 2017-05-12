package se.cs.eventsourcing.domain.store;

import se.cs.eventsourcing.domain.event.DomainEvent;

public class TestObjectFactory {

    public static SomethingChanged newSomethingChanged(String something) {
        return new SomethingChanged(something);
    }

    public static class SomethingChanged implements DomainEvent {

        private final String something;

        public SomethingChanged(String something) {
            this.something = something;
        }

        public String getSomething() {
            return something;
        }
    }
}
