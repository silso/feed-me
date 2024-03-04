package feedme.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.function.BinaryOperator;

public final class TimeUtils {
    // add tests, also I doubt this works for negative durations
    public static Duration multiply(Duration multiplier, Double multiplicand) throws ArithmeticException {
        BigDecimal nanoMultiplier = new BigDecimal(1_000_000_000);
        BigDecimal totalNanos = new BigDecimal(multiplier.getSeconds()).multiply(nanoMultiplier).add(new BigDecimal(multiplier.getNano()));
        BigDecimal product = totalNanos.multiply(new BigDecimal(multiplicand));
        long secondsPart = product.divide(nanoMultiplier, RoundingMode.DOWN).longValueExact();
        long nanosPart = product.remainder(nanoMultiplier).longValueExact();
        return Duration.ofSeconds(secondsPart, nanosPart);
    }

    public static Instant earliest(Instant... times) {
        Instant earliest = Instant.MAX;
        for (Instant time : times) {
            if (time.isBefore(earliest)) {
                earliest = time;
            }
        }
        return earliest;
    }

    public static final BinaryOperator<Instant> earliest = (Instant result, Instant element) -> element.isBefore(result) ? element : result;

    public static Instant latest(Instant... times) {
        Instant latest = Instant.MIN;
        for (Instant time : times) {
            if (time.isAfter(latest)) {
                latest = time;
            }
        }
        return latest;
    }

    public static final BinaryOperator<Instant> latest = (Instant result, Instant element) -> element.isAfter(result) ? element : result;

    public static Duration shortest(Duration... durations) {
        if (durations.length < 1) {
            throw new IllegalArgumentException("Must pass more than zero durations");
        }
        Duration shortest = durations[0];
        for (Duration duration : durations) {
            if (duration.compareTo(shortest) < 0) {
                shortest = duration;
            }
        }
        return shortest;
    }

    public static Duration longest(Duration... durations) {
        if (durations.length < 1) {
            throw new IllegalArgumentException("Must pass more than zero durations");
        }
        Duration longest = durations[0];
        for (Duration duration : durations) {
            if (duration.compareTo(longest) > 0) {
                longest = duration;
            }
        }
        return longest;
    }
}
