package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.store.event.DomainEvent;

public class AmountIncreased implements DomainEvent {

    private final int increaseAmount;

    public AmountIncreased(int increaseAmount) {
        this.increaseAmount = increaseAmount;
    }

    public int getIncreaseAmount() {
        return increaseAmount;
    }

    @Override
    public String toString() {
        return "AmountIncreased{" +
                "increaseAmount=" + increaseAmount +
                '}';
    }
}

