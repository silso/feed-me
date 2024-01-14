package feedme.domain.schedule.timeset;

import com.google.common.base.Objects;
import feedme.util.OptionalUtils;
import feedme.util.TimeUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * The fundamental {@link TimeSet}, defined as the set of all time from (inclusively) the {@link #startTime()} to
 * (exclusively) the {@link #endTime()}. This also has fundamental method implementations for {@link #unionWithTimeSpan(TimeSpan)} and {@link #intersectWithTimeSpan(TimeSpan)}.
 */
public class TimeSpan implements MeasurableTimeSet {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("nn").withZone(ZoneId.systemDefault());
    // TODO: these feel weird
    public static TimeSpan MIN = new TimeSpan(Instant.MIN, Instant.MIN) {
        @Override
        public Instant lastTime() {
            return super.endTime();
        }

        @Override
        public Optional<TimeSpan> getPrevious(Instant time) {
            return Optional.of(this);
        }

        @Override
        public Optional<TimeSpan> getAt(Instant time) {
            return OptionalUtils.fromCondition(
                () -> Instant.MIN.equals(time),
                this
            );
        }

        @Override
        public Optional<TimeSpan> getNext(Instant time) {
            return Optional.empty();
        }
    };
    public static TimeSpan MAX = new TimeSpan(Instant.MAX, Instant.MAX) {
        @Override
        public Instant lastTime() {
            return super.endTime();
        }

        @Override
        public Optional<TimeSpan> getPrevious(Instant time) {
            return Optional.empty();
        }

        @Override
        public Optional<TimeSpan> getAt(Instant time) {
            return OptionalUtils.fromCondition(
                () -> Instant.MAX.equals(time),
                this
            );
        }

        @Override
        public Optional<TimeSpan> getNext(Instant time) {
            return Optional.of(this);
        }
    };
    private final Instant startTime;
    private final Instant endTime;

    protected TimeSpan(@NotNull Instant startTime, @NotNull Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static TimeSpan ofInstant(@NotNull Instant time) {
        if (Instant.MIN.equals(time)) {
            return MIN;
        } else if (Instant.MAX.equals(time)) {
            return MAX;
        } else {
            return new TimeSpan(time, time.plusNanos(1));
        }
    }

    public static TimeSpan ofInstants(@NotNull Instant startTime, @NotNull Instant endTime) {
        if (startTime == endTime) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }
        return new TimeSpan(startTime, endTime);
    }

    private static String formatInstant(Instant time) {
        if (Instant.MIN.equals(time)) {
            return "MIN";
        } else if (Instant.MAX.equals(time)) {
            return "MAX";
        } else {
            return formatter.format(time);
        }
    }

    @Override
    public Optional<TimeSpan> getFirst() {
        return Optional.of(this);
    }

    @Override
    public Optional<TimeSpan> getLast() {
        return Optional.of(this);
    }

    @Override
    public Duration getDuration() {
        return Duration.between(startTime(), endTime());
    }

    public Instant startTime() {
        return startTime;
    }

    public Instant endTime() {
        return endTime;
    }

    public Instant lastTime() {
        return endTime.minusNanos(1);
    }

    @Override
    public Optional<TimeSpan> getPrevious(Instant time) {
        return OptionalUtils.fromCondition(() -> endTime().isBefore(time) || endTime().equals(time), this);
    }

    @Override
    public Optional<TimeSpan> getAt(Instant time) {
        return OptionalUtils.fromCondition(
            () -> startTime().equals(time) || (startTime().isBefore(time) && endTime().isAfter(time)),
            this
        );
    }

    @Override
    public Optional<TimeSpan> getNext(Instant time) {
        return OptionalUtils.fromCondition(() -> startTime().isAfter(time), this);
    }

    public boolean isContiguousWith(TimeSpan other) {
        return hasOverlapWith(other) || contains(other.endTime()) || other.contains(this.endTime());
    }

    public boolean hasOverlapWith(TimeSpan other) {
        return contains(other.startTime()) || other.contains(this.startTime());
    }

    @Override
    public TimeSet unionWith(TimeSet other) {
        return other.unionWithTimeSpan(this);
    }

    @Override
    public MeasurableTimeSet unionWith(MeasurableTimeSet other) {
        return other.unionWithTimeSpan(this);
    }

    @Override
    public MeasurableTimeSet unionWithTimeSpan(TimeSpan other) {
        if (isContiguousWith(other)) {
            return new TimeSpan(
                TimeUtils.earliest(this.startTime(), other.startTime()),
                TimeUtils.latest(this.endTime(), other.endTime())
            );
        } else {
            MutableTimeSet set = MutableTimeSet.create();
            if (!set.addAll(List.of(this, other))) {
                throw new RuntimeException("Failed to add to set");
            }
            return set;
        }
    }

    @Override
    public MeasurableTimeSet intersectWith(TimeSet other) {
        return other.intersectWithTimeSpan(this);
    }

    @Override
    public MeasurableTimeSet intersectWithTimeSpan(TimeSpan other) {
        if (hasOverlapWith(other)) {
            return new TimeSpan(
                TimeUtils.latest(this.startTime(), other.startTime()),
                TimeUtils.earliest(this.endTime(), other.endTime())
            );
        } else {
            return TimeSet.EMPTY;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof TimeSet timeSet) {
            if (timeSet instanceof TimeSpan timeSpan) {
                return Objects.equal(startTime, timeSpan.startTime) && Objects.equal(endTime, timeSpan.endTime);
            } else {
                return timeSet
                    .getAt(startTime)
                    .map(timeSpan -> Objects.equal(startTime, timeSpan.startTime) && Objects.equal(endTime, timeSpan.endTime))
                    .orElse(false)
                    && timeSet.getPrevious(startTime).isEmpty()
                    && timeSet.getNext(lastTime()).isEmpty();
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(startTime, endTime);
    }

    @Override
    public String toString() {
        return "TimeSpan{" +
            formatInstant(startTime()) +
            "-" + formatInstant(endTime()) +
            '}';
    }
}
