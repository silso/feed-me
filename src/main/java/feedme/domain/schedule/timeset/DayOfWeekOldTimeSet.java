package feedme.domain.schedule.timeset;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class DayOfWeekOldTimeSet implements PeriodicTimeSet {
    private final TimeZone localTimeZone;
    private final NumberSpanSet numberSet;

    protected DayOfWeekOldTimeSet(TimeZone localTimeZone, NumberSpanSet numberSet) {
        this.localTimeZone = localTimeZone;
        this.numberSet = numberSet;
    }

    public static TimeSet create(TimeZone localTimeZone, @NotNull DayOfWeek... days) {
        if (days == null || days.length == 0) {
            return TimeSet.EMPTY;
        } else if (days.length == 7) {
            return TimeSet.EVERYTHING;
        } else {
            return null;
        }
    }

    @Override
    public Optional<TimeSpan> getPrevious(Instant time) throws TimeSetException.Unchecked {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(time, localTimeZone.toZoneId());
        NumberSpan span = numberSet.getPrevious(localDateTime.getDayOfWeek().ordinal());
//        return Optional.of(localDateTime.with(TemporalAdjusters.previous(DayOfWeek.TUESDAY)));
        return null;
    }

    @Override
    public Optional<TimeSpan> getAt(Instant time) throws TimeSetException.Unchecked {
//        LocalDateTime localDateTime = LocalDateTime.ofInstant(time, localTimeZone.toZoneId());
//        return OptionalUtils.fromCondition(
//            () -> days.contains(localDateTime.getDayOfWeek()),
//            new TimeSpan(
//
//            )
        return null;
    }

    @Override
    public Optional<TimeSpan> getNext(Instant time) throws TimeSetException.Unchecked {
        return Optional.empty();
    }

    // start and end should be different
    // inclusive on both ends
    protected record DayRange(DayOfWeek start, DayOfWeek end) {
        private boolean loops() {
            return end.ordinal() < start.ordinal();
        }

        private boolean contains(DayOfWeek day) {
            int adjustedStart = 0;
            int adjustedEnd = (end.ordinal() - start.ordinal()) % 7;
            int adjustedDay = (day.ordinal() - start.ordinal()) % 7;
            return adjustedStart <= adjustedDay && adjustedDay <= adjustedEnd;
        }
    }

    protected static class DaySpanSet {
        private final List<DayRange> ranges;

        protected DaySpanSet(List<DayRange> ranges) {
            this.ranges = ranges;
        }

        protected DayRange getPrevious(DayOfWeek day) {
            int closestEnd = 7;
            DayRange closestRange = null;
            for (DayRange range : ranges) {
                int adjustedEnd = (range.end().ordinal() - day.ordinal()) % 7;
                if (adjustedEnd < closestEnd) {
                    closestEnd = adjustedEnd;
                    closestRange = range;
                }
            }
            if (closestRange == null) {
                throw new NullPointerException("Something went wrong when finding previous day range");
            }
            return closestRange;
        }

        protected Optional<DayRange> getAt(DayOfWeek day) {
            for (DayRange range : ranges) {
                if (range.contains(day)) {
                    return Optional.of(range);
                }
            }
            return Optional.empty();
        }
    }

    // inclusive start, exclusive end
    protected record NumberSpan(int start, int end) {}

    protected static class NumberSpanSet {
        private final NavigableSet<NumberSpan> spansByStartTime = new TreeSet<>(Comparator.comparing(NumberSpan::start));
        private final NavigableSet<NumberSpan> spansByEndTime = new TreeSet<>(Comparator.comparing(NumberSpan::end));
        private final int offset;
        private final int length;

        protected NumberSpanSet(@NotNull List<NumberSpan> spans, int offset, int length) {
            spans.forEach(span -> {
                spansByStartTime.add(span);
                spansByEndTime.add(span);
            });
            this.offset = offset;
            this.length = length;
        }

        @NotNull
        protected NumberSpan getPrevious(int number) {
            int numWithOffset = (number - offset) & length;
            NumberSpan span = spansByEndTime.floor(new NumberSpan(numWithOffset, numWithOffset));
            if (null == span) {
                return Objects.requireNonNull(spansByEndTime.pollLast());
            } else {
                return span;
            }
        }

        protected Optional<NumberSpan> getAt(int number) {
            NumberSpan span = spansByStartTime.floor(new NumberSpan(number, number));
            if (span != null && number < span.end()) {
                return Optional.of(span);
            } else {
                return Optional.empty();
            }
        }

        @NotNull
        protected NumberSpan getNext(int number) {
            NumberSpan span = spansByStartTime.higher(new NumberSpan(number, number));
            if (null == span) {
                return Objects.requireNonNull(spansByEndTime.pollFirst());
            } else {
                return span;
            }
        }
    }
}
