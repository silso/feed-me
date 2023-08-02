package feedme.domain.tidbit;

import feedme.domain.tidbit.plan.TidbitPlan;
import feedme.domain.tidbit.plan.impl.FunctionPlan;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;

public class TidbitRepository {
    private final Map<Integer, Tidbit> store = new HashMap<>();

    private int count = 0;
    public int addTidbit(String message) {
        return addTidbit(new Tidbit(
            Instant.now(),
            TidbitState.New,
            new TidbitHistory(new ArrayList<>()),
            message,
            new ArrayList<>(),
            FunctionPlan.exponential(2.1D, 10D, Duration.ofMinutes(5))
        ));
    }

    public int addTidbit(Tidbit tidbit) {
        store.put(++count, tidbit);
        return count;
    }

    public Optional<Tidbit> getTidbit(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean putTidbit(int id, Tidbit tidbit) {
        return store.put(id, tidbit) != null;
    }

    public void forEach(BiConsumer<Integer, Tidbit> consumer) {
        store.forEach(consumer);
    }

    public static class TidbitRepositoryException extends Exception{}
}
