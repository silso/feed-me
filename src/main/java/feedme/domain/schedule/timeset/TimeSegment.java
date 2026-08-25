package feedme.domain.schedule.timeset;

import com.google.common.base.Objects;
import feedme.util.InfInstant;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.jetbrains.annotations.NotNull;

/**
 * The fundamental {@link TimeSet}, defined as the set of all time from (inclusively) the {@link #start()} to
 * (exclusively) the {@link #end()}.
 */
public class TimeSegment extends TimeSpan implements MeasurableTimeSet {
    private final Instant startTime;
    private final Instant endTime;

    protected TimeSegment(@NotNull Instant startTime, @NotNull Instant endTime) {
        super(InfInstant.of(startTime), InfInstant.of(endTime));
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static TimeSegment withBounds(@NotNull Instant startTime, @NotNull Instant endTime) {
        if (startTime == endTime) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }
        return new TimeSegment(startTime, endTime);
    }

    @Override
    public Optional<TimeSegment> measurableGetFirst() {
        return Optional.of(this);
    }

    @Override
    public Optional<TimeSegment> measurableGetLast() {
        return Optional.of(this);
    }

    @Override
    public Duration getDuration() {
        return Duration.between(segmentStart(), segmentEnd());
    }

    public Instant segmentStart() {
        return startTime;
    }

    public Instant segmentEnd() {
        return endTime;
    }

    public boolean isContiguousWith(TimeSegment other) {
        return hasOverlapWith(other) || contains(other.segmentEnd()) || other.contains(this.segmentEnd());
    }

    public boolean hasOverlapWith(TimeSegment other) {
        return contains(other.segmentStart()) || other.contains(this.segmentStart());
    }

    // TODO: more nuanced quality (and hashcode)
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimeSegment that = (TimeSegment) o;
        return startTime.equals(that.startTime) && endTime.equals(that.endTime);
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
            " (" +format(segmentStart()) +
            "-" + format(segmentEnd()) +
            ")}";
    }

    // TODO: fix with proper string conversion. This is a temporary fix since formatter injection
    // currently doesn't work with spock for some reason
    private String format(Instant time) {
        return time.atZone(TimeZone.getDefault().toZoneId()).getDayOfWeek().toString();
    }
}
