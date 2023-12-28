package feedme.domain.schedule.timeset;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public final class TimeSetUtils {
    public static Optional<Instant> findContiguousEndTime(TimeSet a, TimeSet b, Instant from) throws TimeSetException {
        Optional<TimeSpan> aNextMaybe = a.getAt(from);
        Optional<TimeSpan> bNextMaybe = b.getAt(from);
        if (aNextMaybe.isEmpty() && bNextMaybe.isEmpty()) {
            return Optional.empty();
        }

        Instant onTime = from;
        boolean reachedEndTime = false;

        for (int i = 0; i < 1000000; i++) {
            if (aNextMaybe.isPresent()) {
                onTime = aNextMaybe.get().endTime();
                bNextMaybe = b.getAt(onTime);
                aNextMaybe = Optional.empty();
                continue;
            }
            if (bNextMaybe.isPresent()) {
                onTime = bNextMaybe.get().endTime();
                aNextMaybe = a.getAt(onTime);
                bNextMaybe = Optional.empty();
                continue;
            }
            reachedEndTime = true;
            break;
        }

        if (!reachedEndTime) {
            throw new TimeSetException("Couldn't find contiguous end time");
        }
        return Optional.of(onTime);
    }

    public record OptionalSpanOfSet(Optional<TimeSpan> span, TimeSet set) {
        public Stream<SpanOfSet> stream() {
            return span.stream().map(timeSpan -> new SpanOfSet(timeSpan, set));
        }
    }
    public record SpanOfSet(TimeSpan span, TimeSet set) {
        public static Comparator<SpanOfSet> compareEndTimes() {
            return Comparator.comparing(span -> span.span().endTime());
        }
        public static Comparator<SpanOfSet> compareStartTimes() {
            return Comparator.comparing(span -> span.span().startTime());
        }
    }

    private static final long MAX_SEARCH_DEPTH = 1_000;
    private static Instant findNextEmptyTimeRecursive(SpanOfSet currentSpan, long currentDepth, TimeSet... sets) throws TimeSetException.Unchecked {
        if (currentDepth >= MAX_SEARCH_DEPTH) {
            throw new TimeSetException("Ran out of iterations searching for next empty time").unchecked();
        }
        return Stream.of(sets)
            .map(set -> new OptionalSpanOfSet(set.getAt(currentSpan.span().endTime()), set))
            .flatMap(OptionalSpanOfSet::stream)
            .max(SpanOfSet.compareEndTimes())
            .map(span -> findNextEmptyTimeRecursive(span, currentDepth + 1, sets))
            .orElse(currentSpan.span().endTime());
    }

    /**
     * For performing unions
     * @param from time (inclusive) to begin searching from
     * @param sets sets to consider/union together when searching
     * @return resulting first time that is not contained by any of the sets
     * @throws TimeSetException if it can't find one within a bunch of iterations
     */
    public static Instant findNextEmptyTime(Instant from, TimeSet... sets) throws TimeSetException {
        try {
            return Stream.of(sets)
                .map(set -> new OptionalSpanOfSet(set.getAt(from), set))
                .flatMap(OptionalSpanOfSet::stream)
                .max(SpanOfSet.compareEndTimes())
                .map(span -> findNextEmptyTimeRecursive(span, 0, sets))
                .orElse(from);
        } catch (TimeSetException.Unchecked e) {
            throw e.checked();
        }
    }
    private static Instant findPreviousEarliestContainedTimeRecursive(SpanOfSet currentSpan, long currentDepth, TimeSet... sets) throws TimeSetException.Unchecked {
        if (currentDepth >= MAX_SEARCH_DEPTH) {
            throw new TimeSetException("Ran out of iterations searching for previous earliest contained time").unchecked();
        }
        return Stream.of(sets)
            .map(set -> new OptionalSpanOfSet(set.getPreviousContiguousWith(currentSpan.span()), set))
            .flatMap(OptionalSpanOfSet::stream)
            .min(SpanOfSet.compareStartTimes())
            .map(span -> findPreviousEarliestContainedTimeRecursive(span, currentDepth + 1, sets))
            .orElse(currentSpan.span().startTime());
    }

    /**
     * For performing unions
     * @param from time (exclusive) to begin searching from
     * @param sets sets to consider/union together when searching
     * @return resulting first time that is actually contained, but right after an empty time (so like end time of empty time span)
     * @throws TimeSetException if it can't find one within a bunch of iterations
     */
    public static Instant findPreviousEarliestContainedTime(Instant from, TimeSet... sets) throws TimeSetException {
        try {
            return Stream.of(sets)
                .map(set -> new OptionalSpanOfSet(set.getAt(from.minusNanos(1)), set))
                .flatMap(OptionalSpanOfSet::stream)
                .min(SpanOfSet.compareStartTimes())
                .map(span -> findPreviousEarliestContainedTimeRecursive(span, 0, sets))
                .orElse(from);
        } catch (TimeSetException.Unchecked e) {
            throw e.checked();
        }
    }

    public static Instant findNextEmptyTimeIntersect(Instant from, TimeSet... sets) {
        Instant earliestEndTime = from;
        for (int i = 0; i < MAX_SEARCH_DEPTH; i++) {
            Set<OptionalSpanOfSet> currentSpans = new HashSet<>();
            for (TimeSet set : sets) {
                OptionalSpanOfSet optionalSpanOfSet = new OptionalSpanOfSet(set.getAt(earliestEndTime), set);
                currentSpans.add(optionalSpanOfSet);
            }
            if (currentSpans.stream().map(OptionalSpanOfSet::span).allMatch(Optional::isPresent)) {
                earliestEndTime = currentSpans
                    .stream()
                    .flatMap(OptionalSpanOfSet::stream)
                    .map(SpanOfSet::span)
                    .map(TimeSpan::endTime)
                    .min(Comparator.naturalOrder())
                    .orElseThrow();
            } else {
                return earliestEndTime;
            }
        }
        throw new TimeSetException("Couldn't find next empty time within max depth of " + MAX_SEARCH_DEPTH).unchecked();
    }
}
