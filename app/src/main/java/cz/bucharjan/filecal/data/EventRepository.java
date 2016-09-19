package cz.bucharjan.filecal.data;

import java.util.Collection;

import cz.bucharjan.filecal.FileCalException;

/**
 * Provides access to a collection of events in a storage specified by implementation
 */
public interface EventRepository {

    public Collection<Event> getAll() throws FileCalException;

    public void removeAll(Collection<Event> events) throws FileCalException;

    public void insertAll(Collection<Event> events) throws FileCalException;

    void saveChanges() throws FileCalException;
}
