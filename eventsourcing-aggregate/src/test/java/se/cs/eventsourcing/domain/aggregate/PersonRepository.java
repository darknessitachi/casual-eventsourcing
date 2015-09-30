package se.cs.eventsourcing.domain.aggregate;

import se.cs.eventsourcing.domain.store.EventStore;

public class PersonRepository extends AggregateRepository<Person> {

    public PersonRepository(EventStore store) {
        super(store);
    }

    @Override
    protected Class<? extends Person> getAggregateClass() {
        return Person.class;
    }
}
