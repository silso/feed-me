package feedme.domain.schedule.timeset;

import feedme.util.OptionalUtils;

import java.time.Instant;
import java.util.Optional;

public class SimpleResetTimeSet implements PeriodicTimeSet {
    private final Periodic startPeriodic;
    private final Periodic endPeriodic;

    public SimpleResetTimeSet(Periodic startPeriodic, Periodic endPeriodic) {
        this.startPeriodic = startPeriodic;
        this.endPeriodic = endPeriodic;
    }

    @Override
    public Optional<TimeSpan> getPrevious(Instant time) {
        Instant latestPreviousEnd = endPeriodic.getPrevious(time);
        Instant previousStart = startPeriodic.getPrevious(latestPreviousEnd);
        return Optional.of(TimeSpan.ofInstants(
            previousStart,
            endPeriodic.getNext(previousStart)
        ));
    }

    @Override
    public Optional<TimeSpan> getAt(Instant time) {
        Instant latestPreviousStart = startPeriodic.getAt(time).orElse(startPeriodic.getPrevious(time));
        Instant nextEnd = endPeriodic.getNext(latestPreviousStart);
        TimeSpan timeSpan = TimeSpan.ofInstants(latestPreviousStart, nextEnd);
        return OptionalUtils.fromCondition(
            () -> timeSpan.contains(time),
            timeSpan
        );
    }

    @Override
    public Optional<TimeSpan> getNext(Instant time) {
        Instant earliestNextStart = startPeriodic.getNext(time);
        Instant nextEnd = endPeriodic.getNext(earliestNextStart);
        return Optional.of(TimeSpan.ofInstants(
            earliestNextStart,
            nextEnd
        ));
    }
}
