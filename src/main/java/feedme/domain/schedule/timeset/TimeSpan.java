package feedme.domain.schedule.timeset;

import com.google.common.base.Objects;
import feedme.domain.schedule.timeset.operation.TimeSetBinaryOperationVisitor;
import feedme.domain.schedule.timeset.operation.TimeSetUnaryOperationVisitor;
import feedme.util.InfInstant;
import feedme.util.OptionalUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Optional;
import java.util.TimeZone;

/**
 * The fundamental {@link TimeSet}, defined as the set of all time from (inclusively) the {@link #start()} to
 * (exclusive, unless it's +inf) the {@link #end()}.
 */
public class TimeSpan implements CountableTimeSet {
    private final InfInstant startTime;
    private final InfInstant endTime;

    protected TimeSpan(@NotNull Instant startTime, @NotNull Instant endTime) {
        this(InfInstant.of(startTime), InfInstant.of(endTime));
    }

    protected TimeSpan(@NotNull InfInstant startTime, @NotNull InfInstant endTime) {
        if (startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(String.format("Start time must be before end time: '%s' - '%s'", startTime, endTime));
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public Optional<TimeSpan> getPrevious(InfInstant time) {
        if (end().isInfiniteFuture()) {
            return Optional.empty();
        }
        return OptionalUtils.fromCondition(() -> end().isBefore(time) || end().equals(time), this);
    }

    @Override
    public Optional<TimeSpan> getAt(InfInstant time) {
        if (end().isInfiniteFuture() && time.isInfiniteFuture()) {
            return Optional.of(this);
        }
        return OptionalUtils.fromCondition(
            () -> start().equals(time) || (start().isBefore(time) && end().isAfter(time)),
            this
        );
    }

    @Override
    public Optional<TimeSpan> getNext(InfInstant time) {
        return OptionalUtils.fromCondition(() -> start().isAfter(time), this);
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
    public Optional<TimeSpan> getFirst() {
        return Optional.of(this);
    }

    @Override
    public Optional<TimeSpan> getLast() {
        return Optional.of(this);
    }

    public InfInstant start() {
        return startTime;
    }

    public InfInstant end() {
        return endTime;
    }

    public boolean isContiguousWith(TimeSpan other) {
        return hasOverlapWith(other) || contains(other.end()) || other.contains(this.end());
    }

    public boolean hasOverlapWith(TimeSpan other) {
        return contains(other.start()) || other.contains(this.start());
    }

    // TODO: more nuanced equality and hashcode

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TimeSpan timeSpan = (TimeSpan) o;
        return java.util.Objects.equals(startTime, timeSpan.startTime) && java.util.Objects.equals(endTime, timeSpan.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(startTime, endTime);
    }

    @Override
    public String toString() {
        return "TimeSpan{" +
            start() +
            "-" + end() +
            " (" + format(start()) +
            "-" + format(end()) +
            ")}";
    }

    // TODO: fix with proper string conversion. This is a temporary fix since formatter injection
    // currently doesn't work with spock for some reason
    private String format(InfInstant time) {
        if (time.getInstant().isEmpty()) {
            if (time.isInfinitePast()) {
                return "-Inf";
            }
            if (time.isInfiniteFuture()) {
                return "+Inf";
            }
        }
        return time.getInstant().get().atZone(TimeZone.getDefault().toZoneId()).getDayOfWeek().toString();
    }

    public static TimeSpan withBounds(@NotNull Instant startTime, @NotNull Instant endTime) {
        return new TimeSegment(startTime, endTime);
    }

    public static TimeSpan withUpperBound(@NotNull Instant endTime) {
        return new TimeSpan(InfInstant.infinitePast(), InfInstant.of(endTime));
    }

    public static TimeSpan withLowerBound(@NotNull Instant startTime) {
        return new TimeSpan(InfInstant.of(startTime), InfInstant.infiniteFuture());
    }

    public static TimeSpan withBounds(@NotNull InfInstant startTime, @NotNull InfInstant endTime) {
        if (startTime.isFinite() && endTime.isFinite()) {
            return new TimeSegment(startTime.getInstant().get(), endTime.getInstant().get());
        }
        if (startTime.isInfinitePast() && endTime.isInfiniteFuture()) {
            throw new IllegalArgumentException("Use EverythingTimeSet for a TimeSet with everything");
        }
        return new TimeSpan(startTime, endTime);
    }

    public static TimeSpan latestEndTime(@NotNull TimeSpan first, @NotNull TimeSpan second) {
        if (second.end().isAfter(first.end())) {
            return second;
        } else {
            return first;
        }
    }
}
