package me.darksidecode.kantanj.time;

import me.darksidecode.kantanj.types.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExpiringList {

    private final long entryLifespanMillis;

    private final List<Long> entries;

    private final Object lock = new Object();

    public ExpiringList(long singleEntryLifespan, TimeUnit lifespanUnit) {
        if (singleEntryLifespan < 1)
            throw new IllegalArgumentException("invalid singleEntryLifespan: " + singleEntryLifespan);

        entryLifespanMillis = Check.notNull(lifespanUnit,
                "lifespanUnit cannot be null").toMillis(singleEntryLifespan);
        entries = new ArrayList<>();
    }

    public int updateAndCount() {
        synchronized (lock) {
            List<Long> expired = new ArrayList<>();
            entries.stream().filter(e ->
                    System.currentTimeMillis() > (e + entryLifespanMillis)).forEach(expired::add);

            expired.forEach(entries::remove);
            entries.add(System.currentTimeMillis());

            return entries.size();
        }
    }

    /**
     * @deprecated this method returns the current size of the list.
     *             That is, it does not remove any expired entries before.
     *             If a "fresh" list with all outdated entries removed is
     *             desired, updateAndCount() should be used.
     *
     * @see ExpiringList#updateAndCount()
     */
    @Deprecated
    public int count() {
        synchronized (lock) {
            return entries.size();
        }
    }

}
