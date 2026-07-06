package feedme.domain.schedule.timeset;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface MeasurableTimeSet extends TimeSet {
    Optional<TimeSpan> getFirst();
    Optional<TimeSpan> getLast();

    default Duration getDuration() {
//        return Stream.<Optional<TimeSpan>>iterate(
//                getFirst(),
//                (Optional<TimeSpan> currentSpan) -> currentSpan.map(span -> getNext(span.startTime()).isPresent()).orElse(false),
//                (Optional<TimeSpan> currentSpan) -> currentSpan.map(span -> getNext(span.startTime()).orElseThrow().startTime())
//            ).map(this::getAt)
//            .map(Optional::orElseThrow);
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
