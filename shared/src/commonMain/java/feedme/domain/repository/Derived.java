package feedme.domain.repository;

import java.util.function.Supplier;

@FunctionalInterface
public interface Derived<Repository, Value> {
    Value get(Repository repository);

    default Supplier<Value> withRepository(Repository repository) {
        return () -> get(repository);
    }
}
