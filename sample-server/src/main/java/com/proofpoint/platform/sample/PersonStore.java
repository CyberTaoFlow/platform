package com.proofpoint.platform.sample;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import org.weakref.jmx.Flatten;
import org.weakref.jmx.Managed;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class PersonStore
{
    private final ConcurrentMap<String, Person> persons;
    private final PersonStoreStats stats = new PersonStoreStats();

    @Inject
    public PersonStore(StoreConfig config)
    {
        Preconditions.checkNotNull(config, "config must not be null");

        persons = new MapMaker()
                .expiration((long) config.getTtl().toMillis(), TimeUnit.MILLISECONDS)
                .makeMap();
    }

    @Managed
    @Flatten
    public PersonStoreStats getStats()
    {
        return stats;
    }

    public Person get(String id)
    {
        Preconditions.checkNotNull(id, "id must not be null");

        Person person = persons.get(id);
        if (person != null) {
            stats.personFetched();
        }
        return person;
    }

    /**
     * @return true if the entry was created for the first time
     */
    public boolean put(String id, Person person)
    {
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkNotNull(person, "person must not be null");

        boolean added = persons.put(id, person) == null;
        if (added) {
            stats.personAdded();
        }
        else {
            stats.personUpdated();
        }
        return added;
    }

    /**
     * @return true if the entry was removed
     */
    public boolean delete(String id)
    {
        Preconditions.checkNotNull(id, "id must not be null");

        boolean removed = persons.remove(id) != null;
        if (removed) {
            stats.personRemoved();
        }

        return removed;
    }

    public Collection<Person> getAll()
    {
        return ImmutableList.copyOf(persons.values());
    }
}
