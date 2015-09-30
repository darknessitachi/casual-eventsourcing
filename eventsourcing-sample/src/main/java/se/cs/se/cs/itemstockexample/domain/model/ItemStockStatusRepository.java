package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.aggregate.AggregateRepository;
import se.cs.eventsourcing.domain.store.EventStore;

public class ItemStockStatusRepository extends AggregateRepository<ItemStockStatus> {

    public ItemStockStatusRepository(EventStore store) {
        super(store);
    }

    @Override
    protected Class<? extends ItemStockStatus> getAggregateClass() {
        return ItemStockStatus.class;
    }
}
