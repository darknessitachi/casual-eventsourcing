package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

import static com.google.common.base.Preconditions.checkNotNull;

public class NewItemStockStatusCreated extends AbstractDomainEvent {

    private final String name;
    private final int amount;

    public NewItemStockStatusCreated(String name,int amount) {
        super(1);

        this.name = checkNotNull(name);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "NewItemStockStatusCreated{" +
                "name='" + name + '\'' +
                ", amount=" + amount +
                '}';
    }
}
