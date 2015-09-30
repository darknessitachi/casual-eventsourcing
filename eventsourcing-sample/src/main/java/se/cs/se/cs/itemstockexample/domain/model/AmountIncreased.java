package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

public class AmountIncreased extends AbstractDomainEvent {

    private final int increaseAmount;

    public AmountIncreased(int increaseAmount) {
        super(1);

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

