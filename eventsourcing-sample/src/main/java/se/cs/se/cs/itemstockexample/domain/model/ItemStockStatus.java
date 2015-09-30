package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.aggregate.Aggregate;
import se.cs.eventsourcing.domain.aggregate.DomainEventHandler;
import se.cs.eventsourcing.domain.aggregate.EventStreamId;

import static com.google.common.base.Preconditions.checkNotNull;

public class ItemStockStatus extends Aggregate {

    @EventStreamId
    private String id;

    private String name;
    private int amount;

    public ItemStockStatus(String name, int amount) {
        NewItemStockStatusCreated event =
                new NewItemStockStatusCreated(name, amount);

        apply(event);
        append(event);
    }

    @Override
    public String getEventStreamId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public void increase(int amount) {
        AmountIncreased event = new AmountIncreased(amount);
        apply(event);
        append(event);
    }

    public void decrease(int amount) {
        AmountDecreased event = new AmountDecreased(Math.abs(amount));
        apply(event);
        append(event);
    }

    public void changeName(String name) {
        UpdateItemName event = new UpdateItemName(name);
        apply(event);
        append(event);
    }

    @DomainEventHandler
    private void apply(UpdateItemName event) {
        this.name = event.getName();
    }

    @DomainEventHandler
    private void apply(NewItemStockStatusCreated event) {
        this.name = event.getName();
        this.amount = event.getAmount();
    }

    @DomainEventHandler
    private void apply(AmountIncreased event) {
        this.amount += event.getIncreaseAmount();
    }

    @DomainEventHandler
    private void apply(AmountDecreased event) {
        this.amount -= event.getDecreaseAmount();
    }
}
