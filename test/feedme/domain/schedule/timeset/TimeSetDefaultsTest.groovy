package feedme.domain.schedule.timeset

import feedme.domain.schedule.timeset.DurationTimeSet
import feedme.domain.schedule.timeset.FiniteTimeSet
import feedme.domain.schedule.timeset.TimeSet
import feedme.domain.schedule.timeset.TimeSetException
import feedme.domain.schedule.timeset.TimeSpan
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class TimeSetDefaultsTest extends Specification {
    private static nanosAfterEpoch(long nanos) {
        return Instant.EPOCH.plusNanos(nanos)
    }
    private static nanosAfterEpoch(Instant time) {
        return time
    }

    def "UnionWith"() {
    }

    def "UnionWithTimeSpan"() {
    }

    def "IntersectWith"() {
    }

    def "IntersectWithTimeSpan"() {
        when:
        def resultSet = new DurationTimeSet(nanosAfterEpoch(aAnchor), Duration.ofNanos(aBetween), Duration.ofNanos(aOn))
                .intersectWithTimeSpan(TimeSpan.ofInstants(nanosAfterEpoch(bStart), nanosAfterEpoch(bEnd)))

        then:
        resultSet.getPrevious(nanosAfterEpoch(time)).map(res -> previousPresent && res == TimeSpan.ofInstants(nanosAfterEpoch(previousStart), nanosAfterEpoch(previousEnd))).orElse(!previousPresent)
        resultSet.getAt(nanosAfterEpoch(time)).map(res -> atPresent && res == TimeSpan.ofInstants(nanosAfterEpoch(atStart), nanosAfterEpoch(atEnd))).orElse(!atPresent)
        resultSet.getNext(nanosAfterEpoch(time)).map(res -> nextPresent && res == TimeSpan.ofInstants(nanosAfterEpoch(nextStart), nanosAfterEpoch(nextEnd))).orElse(!nextPresent)

        where:
        aAnchor | aBetween | aOn | bStart | bEnd | time || previousPresent | previousStart | previousEnd | atPresent | atStart | atEnd | nextPresent | nextStart | nextEnd
        10      | 20       | 10  | 0      | 1    | -1   || false           | 0             | 0           | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 0      | 1    | 0    || false           | 0             | 0           | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 0      | 1    | 10   || false           | 0             | 0           | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 0      | 1    | 11   || false           | 0             | 0           | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 0      | 10   | -1   || false           | 0             | 0           | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 0      | 10   | 0    || false           | 0             | 0           | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 0      | 10   | 10   || false           | 0             | 0           | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 0      | 10   | 11   || false           | 0             | 0           | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 5      | 11   | -1   || false           | 0             | 0           | false     | 0       | 0     | true        | 10        | 11
        10      | 20       | 10  | 5      | 11   | 0    || false           | 0             | 0           | false     | 0       | 0     | true        | 10        | 11
        10      | 20       | 10  | 5      | 11   | 10   || false           | 0             | 0           | true      | 10      | 11    | false       | 0         | 0
        10      | 20       | 10  | 5      | 11   | 11   || true            | 10            | 11          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 10     | 20   | -1   || false           | 0             | 0           | false     | 0       | 0     | true        | 10        | 20
        10      | 20       | 10  | 10     | 20   | 0    || false           | 0             | 0           | false     | 0       | 0     | true        | 10        | 20
        10      | 20       | 10  | 10     | 20   | 9    || false           | 0             | 0           | false     | 0       | 0     | true        | 10        | 20
        10      | 20       | 10  | 10     | 20   | 10   || false           | 0             | 0           | true      | 10      | 20    | false       | 0         | 0
        10      | 20       | 10  | 10     | 20   | 11   || false           | 0             | 0           | true      | 10      | 20    | false       | 0         | 0
        10      | 20       | 10  | 10     | 20   | 19   || false           | 0             | 0           | true      | 10      | 20    | false       | 0         | 0
        10      | 20       | 10  | 10     | 20   | 20   || true            | 10            | 20          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 10     | 20   | 200  || true            | 10            | 20          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 11     | 16   | -1   || false           | 0             | 0           | false     | 0       | 0     | true        | 11        | 16
        10      | 20       | 10  | 11     | 16   | 0    || false           | 0             | 0           | false     | 0       | 0     | true        | 11        | 16
        10      | 20       | 10  | 11     | 16   | 9    || false           | 0             | 0           | false     | 0       | 0     | true        | 11        | 16
        10      | 20       | 10  | 11     | 16   | 10   || false           | 0             | 0           | false     | 0       | 0     | true        | 11        | 16
        10      | 20       | 10  | 11     | 16   | 11   || false           | 0             | 0           | true      | 11      | 16    | false       | 0         | 0
        10      | 20       | 10  | 11     | 16   | 15   || false           | 0             | 0           | true      | 11      | 16    | false       | 0         | 0
        10      | 20       | 10  | 11     | 16   | 16   || true            | 11            | 16          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 11     | 16   | 20   || true            | 11            | 16          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 11     | 16   | 200  || true            | 11            | 16          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 15     | 21   | -1   || false           | 0             | 0           | false     | 0       | 0     | true        | 15        | 20
        10      | 20       | 10  | 15     | 21   | 0    || false           | 0             | 0           | false     | 0       | 0     | true        | 15        | 20
        10      | 20       | 10  | 15     | 21   | 9    || false           | 0             | 0           | false     | 0       | 0     | true        | 15        | 20
        10      | 20       | 10  | 15     | 21   | 10   || false           | 0             | 0           | false     | 0       | 0     | true        | 15        | 20
        10      | 20       | 10  | 15     | 21   | 11   || false           | 0             | 0           | false     | 0       | 0     | true        | 15        | 20
        10      | 20       | 10  | 15     | 21   | 15   || false           | 0             | 0           | true      | 15      | 20    | false       | 0         | 0
        10      | 20       | 10  | 15     | 21   | 16   || false           | 0             | 0           | true      | 15      | 20    | false       | 0         | 0
        10      | 20       | 10  | 15     | 21   | 20   || true            | 15            | 20          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 15     | 21   | 200  || true            | 15            | 20          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 0      | 30   | -1   || false           | 0             | 0           | false     | 0       | 0     | true        | 10        | 20
        10      | 20       | 10  | 0      | 30   | 0    || false           | 0             | 0           | false     | 0       | 0     | true        | 10        | 20
        10      | 20       | 10  | 0      | 30   | 9    || false           | 0             | 0           | false     | 0       | 0     | true        | 10        | 20
        10      | 20       | 10  | 0      | 30   | 10   || false           | 0             | 0           | true      | 10      | 20    | false       | 0         | 0
        10      | 20       | 10  | 0      | 30   | 11   || false           | 0             | 0           | true      | 10      | 20    | false       | 0         | 0
        10      | 20       | 10  | 0      | 30   | 15   || false           | 0             | 0           | true      | 10      | 20    | false       | 0         | 0
        10      | 20       | 10  | 0      | 30   | 16   || false           | 0             | 0           | true      | 10      | 20    | false       | 0         | 0
        10      | 20       | 10  | 0      | 30   | 20   || true            | 10            | 20          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 0      | 30   | 200  || true            | 10            | 20          | false     | 0       | 0     | false       | 0         | 0
        10      | 20       | 10  | 5      | 35   | -1   || false | 0     | 0     | false | 0     | 0     | true  | 10    | 20
        10      | 20       | 10  | 5      | 35   | 0    || false | 0     | 0     | false | 0     | 0     | true  | 10    | 20
        10      | 20       | 10  | 5      | 35   | 9    || false | 0     | 0     | false | 0     | 0     | true  | 10    | 20
        10      | 20       | 10  | 5      | 35   | 10   || false | 0     | 0     | true  | 10    | 20    | true  | 30    | 35
        10      | 20       | 10  | 5      | 35   | 19   || false | 0     | 0     | true  | 10    | 20    | true  | 30    | 35
        10      | 20       | 10  | 5      | 35   | 20   || true  | 10    | 20    | false | 0     | 0     | true  | 30    | 35
        10      | 20       | 10  | 5      | 35   | 28   || true  | 10    | 20    | false | 0     | 0     | true  | 30    | 35
        10      | 20       | 10  | 5      | 35   | 30   || true  | 10    | 20    | true  | 30    | 35    | false | 0     | 0
        10      | 20       | 10  | 5      | 35   | 35   || true  | 30    | 35    | false | 0     | 0     | false | 0     | 0
        10      | 20       | 10  | 5      | 35   | 200  || true  | 30    | 35    | false | 0     | 0     | false | 0     | 0
    }

    private static Instant MIN = Instant.MIN
    private static Instant MIN1 = Instant.MIN.plusNanos(1)
    private static Instant MAX = Instant.MAX
    private static Instant MAX1 = Instant.MAX.minusNanos(1)

    def "invert TimeSpan with default method"(){
        when:
        def inputSet = new DefaultsTimeSpan(TimeSpan.ofInstants(nanosAfterEpoch(startTime), nanosAfterEpoch(endTime)))
        def resultSet = inputSet.invert()

        then:
        resultSet.invert().getPrevious(nanosAfterEpoch(time)) == inputSet.getPrevious(nanosAfterEpoch(time))
        resultSet.invert().getAt(nanosAfterEpoch(time)) == inputSet.getAt(nanosAfterEpoch(time))
        resultSet.invert().getNext(nanosAfterEpoch(time)) == inputSet.getNext(nanosAfterEpoch(time))
        resultSet.getPrevious(nanosAfterEpoch(time)).map(res -> previousPresent && res == TimeSpan.ofInstants(nanosAfterEpoch(previousStart), nanosAfterEpoch(previousEnd))).orElse(!previousPresent)
        resultSet.getAt(nanosAfterEpoch(time)).map(res -> atPresent && res == TimeSpan.ofInstants(nanosAfterEpoch(atStart), nanosAfterEpoch(atEnd))).orElse(!atPresent)
        resultSet.getNext(nanosAfterEpoch(time)).map(res -> nextPresent && res == TimeSpan.ofInstants(nanosAfterEpoch(nextStart), nanosAfterEpoch(nextEnd))).orElse(!nextPresent)

        where:
        startTime | endTime | time || previousPresent | previousStart | previousEnd | atPresent | atStart | atEnd | nextPresent | nextStart | nextEnd
        0     | 10    | MIN   || false | 0     | 0     | true  | MIN   | 0     | true  | 10    | MAX
        0     | 10    | MIN1  || false | 0     | 0     | true  | MIN   | 0     | true  | 10    | MAX
        0     | 10    | -10   || false | 0     | 0     | true  | MIN   | 0     | true  | 10    | MAX
        0     | 10    | 0     || true  | MIN   | 0     | false | 0     | 0     | true  | 10    | MAX
        0     | 10    | 9     || true  | MIN   | 0     | false | 0     | 0     | true  | 10    | MAX
        0     | 10    | 10    || true  | MIN   | 0     | true  | 10    | MAX   | false | 0     | 0
        0     | 10    | 11    || true  | MIN   | 0     | true  | 10    | MAX   | false | 0     | 0
        0     | 10    | MAX1  || true  | MIN   | 0     | true  | 10    | MAX   | false | 0     | 0
        0     | 10    | MAX   || true  | MIN   | 0     | true  | 10    | MAX   | false | 0     | 0
        9     | 10    | MIN   || false | 0     | 0     | true  | MIN   | 9     | true  | 10    | MAX
        9     | 10    | MIN1  || false | 0     | 0     | true  | MIN   | 9     | true  | 10    | MAX
        9     | 10    | -10   || false | 0     | 0     | true  | MIN   | 9     | true  | 10    | MAX
        9     | 10    | 0     || false | 0     | 0     | true  | MIN   | 9     | true  | 10    | MAX
        9     | 10    | 9     || true  | MIN   | 9     | false | 0     | 0     | true  | 10    | MAX
        9     | 10    | 10    || true  | MIN   | 9     | true  | 10    | MAX   | false | 0     | 0
        9     | 10    | 11    || true  | MIN   | 9     | true  | 10    | MAX   | false | 0     | 0
        9     | 10    | MAX1  || true  | MIN   | 9     | true  | 10    | MAX   | false | 0     | 0
        9     | 10    | MAX   || true  | MIN   | 9     | true  | 10    | MAX   | false | 0     | 0
    }

    private static class DefaultsTimeSpan implements TimeSet {
        private final TimeSpan delegate

        DefaultsTimeSpan(TimeSpan delegate) {
            this.delegate = delegate
        }

        @Override
        Optional<TimeSpan> getPrevious(Instant time) throws TimeSetException.Unchecked {
            return this.delegate.getPrevious(time)
        }

        @Override
        Optional<TimeSpan> getAt(Instant time) throws TimeSetException.Unchecked {
            return this.delegate.getAt(time)
        }

        @Override
        Optional<TimeSpan> getNext(Instant time) throws TimeSetException.Unchecked {
            return this.delegate.getNext(time)
        }

        @Override
        String toString() {
            return delegate.toString()
        }
    }

    def "invert FiniteTimeSet"() {
        given:
        def inputSet = new FiniteTimeSet();

        when:
        inputSet.add(TimeSpan.ofInstants(nanosAfterEpoch(aStart), nanosAfterEpoch(aEnd)))
        inputSet.add(TimeSpan.ofInstants(nanosAfterEpoch(bStart), nanosAfterEpoch(bEnd)))
        def resultSet = inputSet.invert()

        then:
        resultSet.invert().getPrevious(nanosAfterEpoch(time)) == inputSet.getPrevious(nanosAfterEpoch(time))
        resultSet.invert().getAt(nanosAfterEpoch(time)) == inputSet.getAt(nanosAfterEpoch(time))
        resultSet.invert().getNext(nanosAfterEpoch(time)) == inputSet.getNext(nanosAfterEpoch(time))
        resultSet.getPrevious(nanosAfterEpoch(time)).map(res -> previousPresent && res == TimeSpan.ofInstants(nanosAfterEpoch(previousStart), nanosAfterEpoch(previousEnd))).orElse(!previousPresent)
        resultSet.getAt(nanosAfterEpoch(time)).map(res -> atPresent && res == TimeSpan.ofInstants(nanosAfterEpoch(atStart), nanosAfterEpoch(atEnd))).orElse(!atPresent)
        resultSet.getNext(nanosAfterEpoch(time)).map(res -> nextPresent && res == TimeSpan.ofInstants(nanosAfterEpoch(nextStart), nanosAfterEpoch(nextEnd))).orElse(!nextPresent)

        where:
        aStart | aEnd | bStart | bEnd | time || previousPresent | previousStart | previousEnd | atPresent | atStart | atEnd | nextPresent | nextStart | nextEnd
        -5    | 5     | 6     | 10    | MIN   || false | 0     | 0     | true  | MIN   | -5    | true  | 5     | 6
        -5    | 5     | 6     | 10    | -10   || false | 0     | 0     | true  | MIN   | -5    | true  | 5     | 6
        -5    | 5     | 6     | 10    | -5    || true  | MIN   | -5    | false | 0     | 0     | true  | 5     | 6
        -5    | 5     | 6     | 10    | 0     || true  | MIN   | -5    | false | 0     | 0     | true  | 5     | 6
        -5    | 5     | 6     | 10    | 5     || true  | MIN   | -5    | true  | 5     | 6     | true  | 10    | MAX
        -5    | 5     | 6     | 10    | 6     || true  | 5     | 6     | false | 0     | 0     | true  | 10    | MAX
        -5    | 5     | 6     | 10    | 10    || true  | 5     | 6     | true  | 10    | MAX   | false | 0     | 0
        -5    | 5     | 6     | 10    | 11    || true  | 5     | 6     | true  | 10    | MAX   | false | 0     | 0
        -5    | 5     | 6     | 10    | MAX   || true  | 5     | 6     | true  | 10    | MAX   | false | 0     | 0
    }
}
