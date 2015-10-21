# casual-eventsourcing
The purpose of the casual-eventsourcing project is to make life easier for those of you who want to event source things. It is not an event sourcing + CQRS framework - it's probably not a framework at all - but it contains some quite handy features that lets you:

* Model aggregates that track their internal state by appending to a list of domain events
* Persist aggregates - i.e. their internal state - via a repository pattern, backed by an event store
* Persist your events in an underlying jdbc event store (or another technology for that matter if you implement it)
* Replay aggregates via an event stream up to any version
* Upgrade your events, if their structure ever need to change

## License?
MIT, so go nuts!

## How do I use it?
Let's have a look at an example:

```java
// Creates an aggregate repository, backed by an in-memory event store
PersonRepository rep =
        new PersonRepository(new InMemoryEventStore());

LocalDate birthDate = LocalDate.of(1980, 09, 10);

// Creates a new aggregate. Internally, this act generates one domain event
Person person = new Person("Mister", "Bister", Optional.of(birthDate));

// Save the aggregate, i.e. the event stream containing one domain event. The
// aggregate is now at version 1.
rep.save(person);

assertEquals(1, person.version());

// Let's make some changes
person.changeLastName("Last name");
person.changeFirstName("First name");

// Save the aggregate, i.e. append two new domain events to the backing event stream.
rep.save(person);

// Okay, we should now be at version three (version equals the number of events in the stream).
assertEquals(3, person.version());
assertEquals("First name", person.getFirstName());

// Let's fetch an old version of the aggregate, e.g. version 2.
person = rep.find(anEventStreamId, 2).get();

// In this version we have not yet changed the name.
assertEquals(2, person.version());
assertEquals("Mister", person.getFirstName());
```

Looks interesting? Please check the [wiki](https://github.com/indifferen7/casual-eventsourcing/wiki) for more info!

## Roadmap
The current version of casual-eventsourcing is v0.1.0. In the next release we plan to
* add support for loading (i.e. replay) aggregates to any version;
* add snapshot support;
* implement at least one more event store using some cool technology
