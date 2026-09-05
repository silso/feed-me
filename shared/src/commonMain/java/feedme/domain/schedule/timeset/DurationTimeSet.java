package feedme.domain.schedule.timeset;

import feedme.domain.schedule.timeset.operation.TimeSetBinaryOperationVisitor;
import feedme.domain.schedule.timeset.operation.TimeSetUnaryOperationVisitor;
import feedme.util.OptionalUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class DurationTimeSet implements PeriodicTimeSet {
    private final Instant anchor;
    private final Duration timeBetween;
    private final Duration timeOn;

    public DurationTimeSet(@NotNull Instant anchor, @NotNull Duration timeBetween, @NotNull Duration timeOn) {
        if (!timeBetween.isPositive() || !timeOn.isPositive()) {
            throw new IllegalArgumentException("Durations should be positive");
        }
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
    public Optional<TimeSpan> getPrevious(@NotNull Instant time) {
        Instant previousSpanStartTime = getPreviousStartTime(time.minus(timeOn));
        return Optional.of(TimeSpan.withBounds(previousSpanStartTime, previousSpanStartTime.plus(timeOn)));
    }

    @Override
    public Optional<TimeSpan> getAt(@NotNull Instant time) {
        Instant previousStartTime = getPreviousStartTime(time);
        return OptionalUtils.fromCondition(
            () -> Duration.between(previousStartTime, time).minus(timeOn).isNegative(),
            TimeSpan.withBounds(previousStartTime, previousStartTime.plus(timeOn))
        );
    }

    @Override
    public Optional<TimeSpan> getNext(@NotNull Instant time) {
        Instant nextStartTime = getNextStartTime(time);
        return Optional.of(TimeSpan.withBounds(nextStartTime, nextStartTime.plus(timeOn)));
    }

    @Override
    public @NotNull TimeSet accept(TimeSetBinaryOperationVisitor visitor, TimeSet other) {
        return visitor.visit(other, this);
    }

    @Override
    public @NotNull TimeSet accept(TimeSetUnaryOperationVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
