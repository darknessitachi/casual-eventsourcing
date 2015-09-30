package se.cs.eventsourcing.domain.aggregate;

import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

public class TestObjectFactory {

    public static SomethingChanged newSomethingChanged(String something) {
        return new SomethingChanged(something);
    }

    public static class SomethingChanged extends AbstractDomainEvent {

        private final String something;

        public SomethingChanged(String something) {
            super(1);
            this.something = something;
        }

        public String getSomething() {
            return something;
        }
    }
}
