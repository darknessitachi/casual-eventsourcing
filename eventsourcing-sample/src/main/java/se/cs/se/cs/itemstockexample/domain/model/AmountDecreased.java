package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.store.event.DomainEvent;

public class AmountDecreased implements DomainEvent {

    private final int decreaseAmount;

    public AmountDecreased(int decreaseAmount) {
        this.decreaseAmount = decreaseAmount;
    }

    public int getDecreaseAmount() {
        return decreaseAmount;
    }

    @Override
    public String toString() {
        return "AmountDecreased{" +
                "decreaseAmount=" + decreaseAmount +
                '}';
    }
}
