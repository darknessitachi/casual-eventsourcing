package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.store.event.DomainEvent;

import static com.google.common.base.Preconditions.checkNotNull;

public class UpdateItemName implements DomainEvent {

    private String name;

    public UpdateItemName(String name) {
        this.name = checkNotNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "UpdateItemName{" +
                "name='" + name + '\'' +
                '}';
    }
}
