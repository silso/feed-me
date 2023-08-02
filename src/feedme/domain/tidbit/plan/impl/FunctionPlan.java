package feedme.domain.tidbit.plan.impl;

import feedme.domain.tidbit.plan.TidbitPlan;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

public record FunctionPlan(
    Function<Duration, Double> getWhiffNum,
    Function<Double, Duration> inverseGetWhiffNum
) implements TidbitPlan {

    @Override
    public Instant getNextWhiff(Instant currentTime, Duration timeRemaining) {
        if (!timeRemaining.isPositive()) {
            throw new IllegalArgumentException("Time remaining is negative");
        }
        double nextWhiffNum = Math.ceil(getWhiffNum.apply(timeRemaining));
        Duration timeUntilNextWhiff = timeRemaining.minus(inverseGetWhiffNum.apply(nextWhiffNum));
        return currentTime.plus(timeUntilNextWhiff);
    }

    private static final ChronoUnit UNIT = ChronoUnit.HOURS;

    private static Duration durationOfFloat(double duration) {
        long multiplier = FunctionPlan.UNIT.getDuration().dividedBy(ChronoUnit.MILLIS.getDuration());
        return Duration.ofMillis(Math.round(duration * multiplier));
    }

    private static double floatOfDuration(Duration duration) {
        double dividend = FunctionPlan.UNIT.getDuration().dividedBy(ChronoUnit.MILLIS.getDuration());
        return (double) duration.toMillis() / dividend;
    }

    private static double logBase(double x, double base) {
        return Math.log(x) / Math.log(base);
    }

    // should add slight offset and scaling to make this work better, and for limit to be accurate
    public static FunctionPlan exponential(double base, double limit, Duration firstWhiff) {
        // Use function of the form (a * base ^ (-t) + b) for t <= firstWhiff and (-(t - firstWhiff) + 1) for t > firstWhiff
        double a = (1 - limit) / (Math.pow(base, -floatOfDuration(firstWhiff)) - 1);
        double b = limit - a;
        return new FunctionPlan(
            (timeRemaining) -> {
                if (!timeRemaining.minus(firstWhiff).isPositive()) {
                    return a * Math.pow(base, -floatOfDuration(timeRemaining)) + b;
                } else {
                    return -floatOfDuration(timeRemaining.minus(firstWhiff)) + 1;
                }
            },
            (whiffNum) -> {
                if (whiffNum >= 1) {
                    return durationOfFloat(-logBase((whiffNum - b) / a, base));
                } else {
                    return durationOfFloat(floatOfDuration(firstWhiff) + 1 - whiffNum);
                }
            }
        );
    }
}
