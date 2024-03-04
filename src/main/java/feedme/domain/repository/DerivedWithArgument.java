package feedme.domain.repository;

import java.util.function.Function;

@FunctionalInterface
public interface DerivedWithArgument<Repository, Value, Argument> {
    Value get(Repository repository, Argument argument);

    default Function<Argument, Value> withRepository(Repository repository) {
        return (argument) -> get(repository, argument);
    }
}
