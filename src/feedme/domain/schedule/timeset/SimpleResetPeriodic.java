package feedme.domain.schedule.timeset;

import feedme.util.OptionalUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalUnit;
import java.util.Optional;

public class SimpleResetPeriodic implements Periodic {
    private final Period period;
    private final TemporalUnit resetEvery;

    public SimpleResetPeriodic(Period period, TemporalUnit resetEvery) {
        this.period = period;
        this.resetEvery = resetEvery;
    }

    @Override
    public Instant getPrevious(Instant time) {
        Instant resetTime = time.truncatedTo(resetEvery);
        if (resetTime.equals(time)) {
            // previous reset cycle
            resetTime = resetTime.minus(1, resetEvery);
        }
        Duration timeSinceReset = Duration.between(resetTime, time);
        long quotient = timeSinceReset.dividedBy(getDurationOfPeriodAt(resetTime));
        return resetTime.plus(getDurationOfPeriodAt(resetTime).multipliedBy(quotient - 1));
    }

    @Override
    public Optional<Instant> getAt(Instant time) {
        Instant resetTime = time.truncatedTo(resetEvery);
        Duration timeSinceReset = Duration.between(resetTime, time);
        return OptionalUtils.fromCondition(
            () -> timeSinceReset.equals(getDurationOfPeriodAt(resetTime)),
            time
        );
    }

    @Override
    public Instant getNext(Instant time) {
        Instant resetTime = time.truncatedTo(resetEvery);
        Duration timeSinceReset = Duration.between(resetTime, time);
        long quotient = timeSinceReset.dividedBy(getDurationOfPeriodAt(resetTime));
        return resetTime.plus(getDurationOfPeriodAt(resetTime).multipliedBy(quotient + 1));
    }

    public Duration getDurationOfPeriodAt(Instant time) {
        // I think this is incorrect, and I should try to understand Period better
        return Duration.from(period);
    }
}
