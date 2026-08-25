package feedme.domain.schedule.timeset;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

public interface MeasurableTimeSet extends CountableTimeSet {
    Optional<TimeSegment> measurableGetFirst();
    Optional<TimeSegment> measurableGetLast();

    default Duration getDuration() {
        return measurableGetFirst()
            .map(first -> Stream.iterate(
                    first,
                    (TimeSegment span) -> getNext(span.start()).isPresent(),
                    (TimeSegment span) -> getNext(span.start()).map(TimeSegment.class::cast).orElseThrow()
                )
                .map(TimeSegment::getDuration)
                .reduce(Duration.ZERO, Duration::plus)
            )
            .orElse(Duration.ZERO);
    }
}
