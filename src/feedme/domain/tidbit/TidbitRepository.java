package feedme.domain.tidbit;

import feedme.domain.tidbit.action.TidbitAction;
import feedme.domain.tidbit.action.TidbitActionException;
import feedme.domain.tidbit.seed.Seed;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TidbitRepository {
    private final Map<Integer, Tidbit<?>> store = new HashMap<>();

    private int count = 0;

    public int addTidbit(Tidbit<?> tidbit) {
        store.put(++count, tidbit);
        return count;
    }

    public <TidbitType extends Tidbit<?>> Optional<TidbitType> getTidbit(int id, Class<TidbitType> tidbitType) {
        return getTidbit(id).map(tidbitType::cast);
    }

    public Optional<Tidbit<?>> getTidbit(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public <TidbitType extends Tidbit<?>> Map<Integer, TidbitType> getTidbitsForSeed(@NotNull Seed seed, Class<TidbitType> tidbitType) {
        return store
            .entrySet()
            .stream()
            .filter(entry -> seed.equals(entry.getValue().seed))
            .filter(entry -> tidbitType.isAssignableFrom(entry.getValue().getClass()))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> tidbitType.cast(entry.getValue())));
    }

    public boolean putTidbit(int id, Tidbit<?> tidbit) {
        return store.put(id, tidbit) != null;
    }

    public <TidbitType extends Tidbit<?>> boolean applyActionToTidbit(int id, TidbitAction action, Instant now, Class<TidbitType> tidbitType) throws TidbitActionException {
        TidbitType oldTidbit = tidbitType.cast(store.get(id));
        TidbitType newTidbit = action.apply(oldTidbit, now);
        return store.put(id, newTidbit) != null;
    }

    public void forEach(BiConsumer<Integer, Tidbit<?>> consumer) {
        store.forEach(consumer);
    }

    public static class TidbitRepositoryException extends Exception{}
}
