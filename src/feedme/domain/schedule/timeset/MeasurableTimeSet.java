package feedme.domain.schedule.timeset;

import java.time.Duration;
import java.time.Instant;

public interface MeasurableTimeSet extends TimeSet {
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
