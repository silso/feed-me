package feedme.domain.tidbit.seed;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class SeedRepository {
    private final Map<Integer, Seed> store = new HashMap<>();

    private int count = 0;
    private int lastHashCode;

    public synchronized int addSeed(Seed seed) {
        store.put(++count, seed);
        return count;
    }

    public synchronized void forEach(BiConsumer<Integer, Seed> consumer) {
        store.forEach(consumer);
    }

    public synchronized boolean hasChanged() {
        int currentHashCode = Objects.hash(store.entrySet().toArray());
        boolean hasChanged = lastHashCode != currentHashCode;
        lastHashCode = currentHashCode;
        return hasChanged;
    }

    public static class SeedRepositoryException extends Exception{}
}
