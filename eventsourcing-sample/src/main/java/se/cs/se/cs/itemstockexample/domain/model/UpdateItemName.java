package se.cs.se.cs.itemstockexample.domain.model;

import se.cs.eventsourcing.domain.store.AbstractDomainEvent;

import static com.google.common.base.Preconditions.checkNotNull;

public class UpdateItemName extends AbstractDomainEvent {

    private String name;

    public UpdateItemName(String name) {
        super(1);
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
