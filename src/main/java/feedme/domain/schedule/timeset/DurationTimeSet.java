package feedme.domain.schedule.timeset;

import feedme.util.OptionalUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class DurationTimeSet implements PeriodicTimeSet {
    private final Instant anchor;
    private final Duration timeBetween;
    private final Duration timeOn;

    public DurationTimeSet(Instant anchor, Duration timeBetween, Duration timeOn) {
        this.anchor = anchor;
        this.timeBetween = timeBetween;
        this.timeOn = timeOn;
    }

    private Instant getPreviousStartTime(Instant time) {
        Duration timeSinceAnchor = Duration.between(anchor, time);
        if (!timeSinceAnchor.isNegative()) {
            return anchor.plus(timeBetween.multipliedBy(timeSinceAnchor.dividedBy(timeBetween)));
        } else {
            return anchor.plus(timeBetween.multipliedBy(timeSinceAnchor.plusNanos(1).dividedBy(timeBetween) - 1));
        }
    }

    private Instant getNextStartTime(Instant time) {
        Duration timeSinceAnchor = Duration.between(anchor, time);
        if (!timeSinceAnchor.isNegative()) {
            return anchor.plus(timeBetween.multipliedBy(timeSinceAnchor.dividedBy(timeBetween) + 1));
        } else {
            return anchor.plus(timeBetween.multipliedBy(timeSinceAnchor.plusNanos(1).dividedBy(timeBetween)));
        }
    }

    @Override
    public Optional<TimeSpan> getPrevious(Instant time) {
        Instant previousSpanStartTime = getPreviousStartTime(time.minus(timeOn));
        return Optional.of(TimeSpan.ofInstants(previousSpanStartTime, previousSpanStartTime.plus(timeOn)));
    }

    @Override
    public Optional<TimeSpan> getAt(Instant time) {
        Instant previousStartTime = getPreviousStartTime(time);
        return OptionalUtils.fromCondition(
            () -> Duration.between(previousStartTime, time).minus(timeOn).isNegative(),
            TimeSpan.ofInstants(previousStartTime, previousStartTime.plus(timeOn))
        );
    }

    @Override
    public Optional<TimeSpan> getNext(Instant time) {
        Instant nextStartTime = getNextStartTime(time);
        return Optional.of(TimeSpan.ofInstants(nextStartTime, nextStartTime.plus(timeOn)));
    }
}
