package feedme.domain.schedule.timeset;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface MeasurableTimeSet extends TimeSet {
    /**
     * Get the first time span in the time set.
     *
     * @return the first time span in the time set.
     */
    Optional<TimeSpan> getFirst();

    /**
     * Get the last time span in the time set.
     *
     * @return the last time span in the time set.
     */
    Optional<TimeSpan> getLast();

    default Duration getDuration() {
        return streamForwardFrom(Instant.MIN).map(TimeSpan::getDuration).reduce(Duration.ZERO, Duration::plus);
    }

    default MeasurableTimeSet unionWith(MeasurableTimeSet other) {
        return (MeasurableTimeSet) TimeSet.super.unionWith(other);
    }

    @Override
    default MeasurableTimeSet unionWithTimeSpan(TimeSpan other) {
        return (MeasurableTimeSet) TimeSet.super.unionWithTimeSpan(other);
    }

    @Override
    default MeasurableTimeSet intersectWith(TimeSet other) {
        return (MeasurableTimeSet) TimeSet.super.intersectWith(other);
    }
}
