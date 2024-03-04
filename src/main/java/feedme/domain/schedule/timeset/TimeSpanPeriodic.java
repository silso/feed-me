package feedme.domain.schedule.timeset;

import feedme.util.TimeUtils;

import java.time.Instant;
import java.util.Arrays;

public interface TimeSpanPeriodic {
    /**
     * @param after The time that occurs after (exclusive) the time span returned.
     * @return The previous (exclusive) time span in this periodic.
     */
    TimeSpan getPrev(Instant after);

    /**
     * @param before The time that occurs before (inclusive) the time span returned.
     * @return The next (inclusive) time span in this periodic.
     */
    TimeSpan getNext(Instant before);

    static TimeSpanPeriodic union(TimeSpanPeriodic... periodics) {
        return new TimeSpanPeriodic() {
            @Override
            public TimeSpan getPrev(Instant after) {
                return TimeSpan.ofInstants(
                    getStartTime(after, periodics),
                    Arrays
                        .stream(periodics)
                        .map(periodic -> periodic.getPrev(after))
                        .map(TimeSpan::endTime)
                        .reduce(TimeUtils.latest)
                        .orElseThrow()
                );
            }

            // somewhat ugly recursion that might be wrong especially on edge cases
            private Instant getStartTime(Instant time, TimeSpanPeriodic... periodics) {
                return Arrays
                    .stream(periodics)
                    .map(periodic -> periodic.getNext(time))
                    .filter(timeSpan -> timeSpan.contains(time))
                    .filter(timeSpan -> timeSpan.startTime().isBefore(time))
                    .findFirst()
                    .map(TimeSpan::startTime)
                    .map(newTime -> getStartTime(newTime, periodics))
                    .orElse(time);
            }

            @Override
            public TimeSpan getNext(Instant before) {
                return TimeSpan.ofInstants(
                    Arrays
                        .stream(periodics)
                        .map(periodic -> periodic.getNext(before))
                        .map(TimeSpan::startTime)
                        .reduce(TimeUtils.earliest)
                        .orElseThrow(),
                    getEndTime(before, periodics)
                );
            }

            // ditto
            private Instant getEndTime(Instant time, TimeSpanPeriodic... periodics) {
                return Arrays
                    .stream(periodics)
                    .map(periodic -> periodic.getNext(time))
                    .filter(timeSpan -> timeSpan.contains(time))
                    .filter(timeSpan -> timeSpan.endTime().isAfter(time))
                    .findFirst()
                    .map(TimeSpan::endTime)
                    .map(newTime -> getEndTime(newTime, periodics))
                    .orElse(time);
            }
        };
    }
}
