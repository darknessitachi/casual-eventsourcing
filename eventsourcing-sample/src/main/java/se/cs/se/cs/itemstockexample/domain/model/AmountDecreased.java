package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

public class AmountDecreased extends AbstractDomainEvent {

    private final int decreaseAmount;

    public AmountDecreased(int decreaseAmount) {
        super(1);

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
