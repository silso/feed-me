package feedme.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public final class OptionalUtils {
    public static <T> Optional<T> fromCondition(BooleanSupplier predicate, @NotNull T mappedValue) {
        if (predicate.getAsBoolean()) {
            return Optional.of(mappedValue);
        } else {
            return Optional.empty();
        }
    }
}
