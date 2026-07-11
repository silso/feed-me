package feedme.domain.schedule.timeset;

import feedme.util.TimeUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * A simple way to compose multiple {@link TimeSpan}s using sets.
 */
public class FiniteTimeSet implements MutableTimeSet, MeasurableTimeSet {
    private final NavigableMap<Instant, TimeSpan> spansByStartTime = new TreeMap<>();
    private final NavigableMap<Instant, TimeSpan> spansByEndTime = new TreeMap<>();

    public FiniteTimeSet() {}

    @Override
    public Optional<TimeSpan> getPrevious(Instant time) {
        return Optional.ofNullable(spansByEndTime.floorEntry(time)).map(Map.Entry::getValue);
    }

    @Override
    public Optional<TimeSpan> getAt(Instant time) {
        // inclusive
        Optional<TimeSpan> latestPreviousStart = Optional.ofNullable(spansByStartTime.floorEntry(time)).map(Map.Entry::getValue);
        // exclusive
        Optional<TimeSpan> earliestNextEnd = Optional.ofNullable(spansByEndTime.higherEntry(time)).map(Map.Entry::getValue);
        if (latestPreviousStart.isPresent() && latestPreviousStart.equals(earliestNextEnd)) {
            return latestPreviousStart;
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TimeSpan> getNext(Instant time) {
        return Optional.ofNullable(spansByStartTime.higherEntry(time)).map(Map.Entry::getValue);
    }

    @Override
    public TimeSet unionWith(TimeSet other) {
        TimeSet set = other;
        for (TimeSpan span : spansByStartTime.values()) {
            set = set.unionWithTimeSpan(span);
        }
        return set;
    }

    @Override
    public MeasurableTimeSet unionWith(MeasurableTimeSet other) {
        MeasurableTimeSet set = other;
        for (TimeSpan span : spansByStartTime.values()) {
            set = set.unionWithTimeSpan(span);
        }
        return set;
    }

    @Override
    public MeasurableTimeSet unionWithTimeSpan(TimeSpan other) {
        MutableTimeSet set = MutableTimeSet.create();
        Set<TimeSpan> contiguousTimeSpans = new HashSet<>();
        for (TimeSpan span : spansByStartTime.values()) {
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
        for (TimeSpan span : spansByStartTime.values()) {
            set = set.unionWith(other.intersectWithTimeSpan(span));
        }
        return set;
    }

    @Override
    public MeasurableTimeSet intersectWithTimeSpan(TimeSpan other) {
        MeasurableTimeSet set = TimeSet.EMPTY;
        Set<TimeSpan> contiguousTimeSpans = new HashSet<>();
        for (TimeSpan span : spansByStartTime.values()) {
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
    public synchronized void add(TimeSpan span) {
        // this is slow, should find a better way to do this
        if (spansByStartTime.values().stream().anyMatch(s -> s.isContiguousWith(span))) {
            throw new IllegalArgumentException("Can't add contiguous span to set");
        }
        spansByStartTime.put(span.startTime(), span);
        spansByEndTime.put(span.endTime(), span);
    }

    @Override
    public void addAll(Collection<TimeSpan> spans) {
        spans.forEach(this::add);
    }

    @Override
    public synchronized void remove(TimeSpan span) {
        spansByStartTime.remove(span.startTime());
        spansByEndTime.remove(span.endTime());
    }

    @Override
    public Optional<TimeSpan> getFirst() {
        return Optional.ofNullable(spansByStartTime.firstEntry()).map(Map.Entry::getValue);
    }

    @Override
    public Optional<TimeSpan> getLast() {
        return Optional.ofNullable(spansByEndTime.lastEntry()).map(Map.Entry::getValue);
    }

    @Override
    public Duration getDuration() {
        return spansByStartTime.values().stream().map(TimeSpan::getDuration).reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof FiniteTimeSet that) {
            return com.google.common.base.Objects.equal(spansByStartTime, that.spansByStartTime) && com.google.common.base.Objects.equal(spansByEndTime, that.spansByEndTime);
        } else {
            if (o instanceof TimeSpan that && spansByStartTime.size() == 1) {
                return spansByStartTime.containsValue(that);
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
