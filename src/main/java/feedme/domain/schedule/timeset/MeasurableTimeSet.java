package feedme.domain.schedule.timeset;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

public interface MeasurableTimeSet extends TimeSet {
    Optional<TimeSpan> getFirst();
    Optional<TimeSpan> getLast();

    default Duration getDuration() {
        return getFirst()
            .map(_span -> Stream.iterate(
                    getFirst().get(),
                    (TimeSpan span) -> getNext(span.startTime()).isPresent(),
                    (TimeSpan span) -> getNext(span.startTime()).orElseThrow()
                )
                .map(TimeSpan::getDuration)
                .reduce(Duration.ZERO, Duration::plus)
            )
            .orElse(Duration.ZERO);
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
