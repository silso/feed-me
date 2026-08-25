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
                onTime = aNextMaybe.get().end().getInstantOrElseThrow();
                bNextMaybe = b.getAt(onTime);
                aNextMaybe = Optional.empty();
                continue;
            }
            if (bNextMaybe.isPresent()) {
                onTime = bNextMaybe.get().end().getInstantOrElseThrow();
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
            return Comparator.comparing(span -> span.span().end());
        }
        public static Comparator<SpanOfSet> compareStartTimes() {
            return Comparator.comparing(span -> span.span().start());
        }
    }

    private static final long MAX_SEARCH_DEPTH = 1_000;
    private static Instant findNextEmptyTimeRecursive(SpanOfSet currentSpan, long currentDepth, TimeSet... sets) throws TimeSetException.Unchecked {
        if (currentDepth >= MAX_SEARCH_DEPTH) {
            throw new TimeSetException("Ran out of iterations searching for next empty time").unchecked();
        }
        return Stream.of(sets)
            .map(set -> new OptionalSpanOfSet(set.getAt(currentSpan.span().end().getInstantOrElseThrow()), set))
            .flatMap(OptionalSpanOfSet::stream)
            .max(SpanOfSet.compareEndTimes())
            .map(span -> findNextEmptyTimeRecursive(span, currentDepth + 1, sets))
            .orElse(currentSpan.span().end().getInstantOrElseThrow());
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
                .filter(span -> !span.span().end().isInfiniteFuture())
                .map(span -> findNextEmptyTimeRecursive(span, 0, sets))
                .orElse(from);
        } catch (TimeSetException.Unchecked e) {
            throw e.checked();
        }
    }
}
