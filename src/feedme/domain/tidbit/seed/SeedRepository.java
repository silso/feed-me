package feedme.domain.tidbit.seed;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SeedRepository {
    private final Map<Integer, Seed> store = new HashMap<>();

    private int count = 0;

    public int addSeed(Seed seed) {
        store.put(++count, seed);
        return count;
    }

    public void forEach(BiConsumer<Integer, Seed> consumer) {
        store.forEach(consumer);
    }

    public static class SeedRepositoryException extends Exception{}
}
