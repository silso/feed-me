package feedme.domain.schedule.timeset;

import feedme.util.InfInstant;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * A simple way to compose multiple {@link TimeSegment}s using sets.
 */
public class TimeSegmentSet extends TimeSpanSet implements MutableTimeSet, MeasurableTimeSet {
    private final NavigableMap<Instant, TimeSegment> spansByStartTime = new TreeMap<>();
    private final NavigableMap<Instant, TimeSegment> spansByEndTime = new TreeMap<>();

    public TimeSegmentSet() {}

    @Override
    public Optional<TimeSpan> getPrevious(InfInstant time) {
        if (time.isInfiniteFuture()) {
            return getLast();
        }
        if (time.isInfinitePast()) {
            return Optional.empty();
        }
        return Optional.ofNullable(spansByEndTime.floorEntry(time.getInstant().get())).map(Map.Entry::getValue);
    }

    @Override
    public Optional<TimeSpan> getAt(InfInstant time) {
        if (!time.isFinite()) {
            return Optional.empty();
        }
        // inclusive
        Optional<TimeSpan> latestPreviousStart = Optional.ofNullable(spansByStartTime.floorEntry(time.getInstant().get())).map(Map.Entry::getValue);
        // exclusive
        Optional<TimeSpan> earliestNextEnd = Optional.ofNullable(spansByEndTime.higherEntry(time.getInstant().get())).map(Map.Entry::getValue);
        if (latestPreviousStart.isPresent() && latestPreviousStart.equals(earliestNextEnd)) {
            return latestPreviousStart;
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TimeSpan> getNext(InfInstant time) {
        if (time.isInfinitePast()) {
            return getFirst();
        }
        if (time.isInfiniteFuture()) {
            return Optional.empty();
        }
        return Optional.ofNullable(spansByStartTime.higherEntry(time.getInstant().get())).map(Map.Entry::getValue);
    }

    @Override
    public synchronized void add(TimeSpan span) {
        if (!(span instanceof TimeSegment segment)) {
            throw new IllegalArgumentException("Can only add TimeSegment to TimeSegmentSet");
        }
        // this is slow, should find a better way to do this
        if (spansByStartTime.values().stream().anyMatch(s -> s.isContiguousWith(segment))) {
            throw new IllegalArgumentException("Can't add contiguous span to set");
        }
        spansByStartTime.put(segment.segmentStart(), segment);
        spansByEndTime.put(segment.segmentEnd(), segment);
    }

    @Override
    public void addAll(Collection<TimeSpan> spans) {
        spans.forEach(this::add);
    }

    @Override
    public synchronized void remove(TimeSpan span) {
        if (!(span instanceof TimeSegment segment)) {
            throw new IllegalArgumentException("Can only remove TimeSegment from TimeSegmentSet");
        }
        spansByStartTime.remove(segment.segmentStart());
        spansByEndTime.remove(segment.segmentEnd());
    }

    @Override
    public Optional<TimeSegment> measurableGetFirst() {
        return Optional.ofNullable(spansByStartTime.firstEntry()).map(Map.Entry::getValue);
    }

    @Override
    public Optional<TimeSegment> measurableGetLast() {
        return Optional.ofNullable(spansByEndTime.lastEntry()).map(Map.Entry::getValue);
    }

    @Override
    public Duration getDuration() {
        return spansByStartTime.values().stream().map(TimeSegment::getDuration).reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof TimeSegmentSet that) {
            return com.google.common.base.Objects.equal(spansByStartTime, that.spansByStartTime) && com.google.common.base.Objects.equal(spansByEndTime, that.spansByEndTime);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(spansByStartTime, spansByEndTime);
    }

    @Override
    public String toString() {
        return "TimeSegmentSet{" +
            spansByStartTime +
            '}';
    }
}
