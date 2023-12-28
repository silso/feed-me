package feedme.domain.schedule.timeset;

import feedme.util.TimeUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * A simple way to compose multiple {@link TimeSpan}s using sets.
 */
public class FiniteTimeSet implements MutableTimeSet, MeasurableTimeSet {
    private final NavigableSet<TimeSpan> spansByStartTime = new TreeSet<>(Comparator.comparing(TimeSpan::startTime));
    private final NavigableSet<TimeSpan> spansByEndTime = new TreeSet<>(Comparator.comparing(TimeSpan::endTime));

    public FiniteTimeSet() {}

    @Override
    public Optional<TimeSpan> getPrevious(Instant time) {
        return Optional.ofNullable(spansByEndTime.floor(TimeSpan.ofInstant(time)));
    }

    @Override
    public Optional<TimeSpan> getAt(Instant time) {
        // inclusive
        @Nullable TimeSpan latestPreviousStart = spansByStartTime.floor(TimeSpan.ofInstant(time));
        // exclusive
        @Nullable TimeSpan earliestNextEnd = spansByEndTime.higher(TimeSpan.ofInstant(time));
        if (latestPreviousStart != null && latestPreviousStart.equals(earliestNextEnd)) {
            return Optional.of(latestPreviousStart);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TimeSpan> getNext(Instant time) {
        return Optional.ofNullable(spansByStartTime.higher(TimeSpan.ofInstant(time)));
    }

    @Override
    public TimeSet unionWith(TimeSet other) {
        TimeSet set = other;
        for (TimeSpan span : spansByStartTime) {
            set = set.unionWithTimeSpan(span);
        }
        return set;
    }

    @Override
    public MeasurableTimeSet unionWith(MeasurableTimeSet other) {
        MeasurableTimeSet set = other;
        for (TimeSpan span : spansByStartTime) {
            set = set.unionWithTimeSpan(span);
        }
        return set;
    }

    @Override
    public MeasurableTimeSet unionWithTimeSpan(TimeSpan other) {
        MutableTimeSet set = MutableTimeSet.create();
        Set<TimeSpan> contiguousTimeSpans = new HashSet<>();
        for (TimeSpan span : spansByStartTime) {
            if (span.isContiguousWith(other)) {
                contiguousTimeSpans.add(span);
            } else {
                set.add(span);
            }
        }
        set.add(TimeSpan.ofInstants(
            TimeUtils.earliest(contiguousTimeSpans.stream().map(TimeSpan::startTime).toArray(Instant[]::new)),
            TimeUtils.latest(contiguousTimeSpans.stream().map(TimeSpan::endTime).toArray(Instant[]::new))
        ));
        return set;
    }

    @Override
    public MeasurableTimeSet intersectWith(TimeSet other) {
        MeasurableTimeSet set = TimeSet.EMPTY;
        for (TimeSpan span : spansByStartTime) {
            set = set.unionWith(other.intersectWithTimeSpan(span));
        }
        return set;
    }

    @Override
    public MeasurableTimeSet intersectWithTimeSpan(TimeSpan other) {
        MeasurableTimeSet set = TimeSet.EMPTY;
        Set<TimeSpan> contiguousTimeSpans = new HashSet<>();
        for (TimeSpan span : spansByStartTime) {
            if (span.hasOverlapWith(other)) {
                contiguousTimeSpans.add(span);
            }
        }
        for (TimeSpan span : contiguousTimeSpans) {
            set = set.unionWith(span.intersectWithTimeSpan(other));
        }
        return set;
    }

    @Override
    public synchronized boolean add(TimeSpan span) {
        // this is slow, should find a better way to do this
        if (spansByStartTime.stream().anyMatch(s -> s.isContiguousWith(span))) {
            throw new IllegalArgumentException("Can't add contiguous span to set");
        }
        return spansByStartTime.add(span) & spansByEndTime.add(span);
    }

    @Override
    public boolean addAll(Collection<TimeSpan> spans) {
        return spans.stream().allMatch(this::add);
    }

    @Override
    public synchronized boolean remove(TimeSpan span) {
        return spansByStartTime.remove(span) & spansByEndTime.remove(span);
    }

    @Override
    public Duration getDuration() {
        return spansByStartTime.stream().map(TimeSpan::getDuration).reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof FiniteTimeSet that) {
            return com.google.common.base.Objects.equal(spansByStartTime, that.spansByStartTime) && com.google.common.base.Objects.equal(spansByEndTime, that.spansByEndTime);
        } else {
            if (o instanceof TimeSpan that && spansByStartTime.size() == 1) {
                return spansByStartTime.contains(that);
            } else {
                return false;
            }
        }
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(spansByStartTime, spansByEndTime);
    }

    @Override
    public String toString() {
        return "FiniteTimeSet{" +
            spansByStartTime +
            '}';
    }
}
