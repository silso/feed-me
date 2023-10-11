package feedme.domain.tidbit;

import feedme.domain.tidbit.action.TidbitAction;
import feedme.domain.tidbit.action.TidbitActionException;
import feedme.domain.tidbit.seed.Seed;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Interface for storing, retrieving, and modifying tidbits. Currently, the tidbits are stored in memory.
 */
public class TidbitRepository {
    private final Map<Integer, Tidbit> store = new HashMap<>();

    private int count = 0;
    private int lastHashCode;

    public synchronized int addTidbit(Tidbit tidbit) {
        store.put(++count, tidbit);
        return count;
    }

    public synchronized <TidbitType extends Tidbit> Optional<TidbitType> getTidbit(int id, Class<TidbitType> tidbitType) {
        return getTidbit(id).map(tidbitType::cast);
    }

    public synchronized Optional<Tidbit> getTidbit(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public synchronized <TidbitType extends Tidbit> Map<Integer, TidbitType> getTidbitsForSeed(@NotNull Seed seed, Class<TidbitType> tidbitType) {
        return store
            .entrySet()
            .stream()
            .filter(entry -> seed.equals(entry.getValue().seed))
            .filter(entry -> tidbitType.isAssignableFrom(entry.getValue().getClass()))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> tidbitType.cast(entry.getValue())));
    }

    public synchronized boolean putTidbit(int id, Tidbit tidbit) {
        return store.put(id, tidbit) != null;
    }

    public synchronized <TidbitType extends Tidbit> boolean applyActionToTidbit(int id, TidbitAction action, Instant now, Class<TidbitType> tidbitType) throws TidbitActionException {
        TidbitType oldTidbit = tidbitType.cast(store.get(id));
        TidbitType newTidbit = action.apply(oldTidbit, now);
        return store.put(id, newTidbit) != null;
    }

    public synchronized void forEach(BiConsumer<Integer, Tidbit> consumer) {
        store.forEach(consumer);
    }

    public synchronized boolean hasChanged() {
        int currentHashCode = Objects.hash(store.entrySet().toArray());
        boolean hasChanged = lastHashCode != currentHashCode;
        lastHashCode = currentHashCode;
        return hasChanged;
    }

    public static class TidbitRepositoryException extends Exception{}
}
