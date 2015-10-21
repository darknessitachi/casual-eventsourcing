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
Please check the [wiki](https://github.com/indifferen7/casual-eventsourcing/wiki)!

## Roadmap
The current version of casual-eventsourcing is v0.1.0. In the next release we plan to
* add support for loading (i.e. replay) aggregates to any version;
* add snapshot support;
* implement at least one more event store using some cool technology
