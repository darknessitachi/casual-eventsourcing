package se.cs.eventsourcing.domain.changeset;

import se.cs.eventsourcing.domain.event.DomainEvent;

public class SomeEvent implements DomainEvent {

    private String someString;
    private int someInt;

    private SomeEvent() {}

    public SomeEvent(String someString, int someInt) {
        this.someString = someString;
        this.someInt = someInt;
    }

    public String getSomeString() {
        return someString;
    }

    public int getSomeInt() {
        return someInt;
    }

    @Override
    public String toString() {
        return "SomeEvent{" +
                "someString='" + someString + '\'' +
                ", someInt=" + someInt +
                '}';
    }
}

