package feedme.domain.schedule.timeset;

import feedme.domain.schedule.timeset.operation.TimeSetBinaryOperationVisitor;
import feedme.domain.schedule.timeset.operation.TimeSetUnaryOperationVisitor;
import feedme.util.InfInstant;
import java.util.*;
import org.jetbrains.annotations.NotNull;

/**
 * A simple way to compose multiple {@link TimeSpan}s using sets.
 */
public class TimeSpanSet implements MutableTimeSet {
    private final NavigableMap<InfInstant, TimeSpan> spansByStartTime = new TreeMap<>();
    private final NavigableMap<InfInstant, TimeSpan> spansByEndTime = new TreeMap<>();

    public TimeSpanSet() {}

    @Override
    public Optional<TimeSpan> getPrevious(InfInstant time) {
        return Optional.ofNullable(spansByEndTime.floorEntry(time)).map(Map.Entry::getValue);
    }

    @Override
    public Optional<TimeSpan> getAt(InfInstant time) {
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
    public Optional<TimeSpan> getNext(InfInstant time) {
        return Optional.ofNullable(spansByStartTime.higherEntry(time)).map(Map.Entry::getValue);
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
    public synchronized void add(TimeSpan span) {
        // this is slow, should find a better way to do this
        if (spansByStartTime.values().stream().anyMatch(s -> s.isContiguousWith(span))) {
            throw new IllegalArgumentException("Can't add contiguous span to set");
        }
        spansByStartTime.put(span.start(), span);
        spansByEndTime.put(span.end(), span);
    }

    @Override
    public void addAll(Collection<TimeSpan> spans) {
        spans.forEach(this::add);
    }

    @Override
    public synchronized void remove(TimeSpan span) {
        spansByStartTime.remove(span.start());
        spansByEndTime.remove(span.end());
    }

    @Override
    public int size() {
        return spansByStartTime.size();
    }

    @Override
    public boolean isEmpty() {
        return spansByStartTime.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof TimeSpanSet that) {
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
        return "TimeSpanSet{" +
            spansByStartTime +
            '}';
    }
}
