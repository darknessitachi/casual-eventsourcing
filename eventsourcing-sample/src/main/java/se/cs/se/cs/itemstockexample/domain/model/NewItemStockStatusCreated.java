package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.store.event.DomainEvent;

import static com.google.common.base.Preconditions.checkNotNull;

public class NewItemStockStatusCreated implements DomainEvent {

    private final String name;
    private final int amount;

    public NewItemStockStatusCreated(String name,int amount) {
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
