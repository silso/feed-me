package feedme.domain.schedule.timeset;

import feedme.util.OptionalUtils;

import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

// TODO: remove?
public class SimpleResetTimeSet {
    private final Periodic startPeriodic;
    private final Periodic endPeriodic;

    public SimpleResetTimeSet(Periodic startPeriodic, Periodic endPeriodic) {
        this.startPeriodic = startPeriodic;
        this.endPeriodic = endPeriodic;
    }

    public Optional<TimeSpan> getPrevious(@NotNull Instant time) {
        Instant latestPreviousEnd = endPeriodic.getPrevious(time);
        Instant previousStart = startPeriodic.getPrevious(latestPreviousEnd);
        return Optional.of(TimeSpan.withBounds(
            previousStart,
            endPeriodic.getNext(previousStart)
        ));
    }

    public Optional<TimeSpan> getAt(@NotNull Instant time) {
        Instant latestPreviousStart = startPeriodic.getAt(time).orElse(startPeriodic.getPrevious(time));
        Instant nextEnd = endPeriodic.getNext(latestPreviousStart);
        TimeSpan timeSpan = TimeSpan.withBounds(latestPreviousStart, nextEnd);
        return OptionalUtils.fromCondition(
            () -> timeSpan.contains(time),
            timeSpan
        );
    }

    public Optional<TimeSpan> getNext(@NotNull Instant time) {
        Instant earliestNextStart = startPeriodic.getNext(time);
        Instant nextEnd = endPeriodic.getNext(earliestNextStart);
        return Optional.of(TimeSpan.withBounds(
            earliestNextStart,
            nextEnd
        ));
    }
}
