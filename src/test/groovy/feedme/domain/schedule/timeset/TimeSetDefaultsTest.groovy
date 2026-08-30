package feedme.domain.schedule.timeset

import feedme.domain.schedule.timeset.operation.TimeSetBinaryOperationVisitor
import feedme.domain.schedule.timeset.operation.TimeSetUnaryOperationVisitor
import feedme.util.InfInstant
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class TimeSetDefaultsTest extends Specification {
    private static nanosAfterEpoch(long nanos) {
        return Instant.EPOCH.plusNanos(nanos)
    }
    private static nanosAfterEpochInf(long nanos) {
        return InfInstant.of(Instant.EPOCH.plusNanos(nanos))
    }
    private static nanosAfterEpochInf(InfInstant time) {
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
                .intersectWith(TimeSpan.withBounds(nanosAfterEpoch(bStart), nanosAfterEpoch(bEnd)))

        then:
        resultSet.getPrevious(nanosAfterEpoch(time)).map(res -> previousPresent && res == TimeSpan.withBounds(nanosAfterEpoch(previousStart), nanosAfterEpoch(previousEnd))).orElse(!previousPresent)
        resultSet.getAt(nanosAfterEpoch(time)).map(res -> atPresent && res == TimeSpan.withBounds(nanosAfterEpoch(atStart), nanosAfterEpoch(atEnd))).orElse(!atPresent)
        resultSet.getNext(nanosAfterEpoch(time)).map(res -> nextPresent && res == TimeSpan.withBounds(nanosAfterEpoch(nextStart), nanosAfterEpoch(nextEnd))).orElse(!nextPresent)

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

    private static InfInstant MIN = InfInstant.infinitePast()
    private static InfInstant MIN1 = InfInstant.of(Instant.MIN.plusNanos(1))
    private static InfInstant MAX = InfInstant.infiniteFuture()
    private static InfInstant MAX1 = InfInstant.of(Instant.MAX.minusNanos(1))

    def "invert TimeSpan with default method"(){
        when:
        def inputSet = new DefaultsTimeSpan(TimeSpan.withBounds(nanosAfterEpochInf(startTime), nanosAfterEpochInf(endTime)))
        def resultSet = inputSet.invert()

        then:
        resultSet.invert().getPrevious(nanosAfterEpochInf(time)) == inputSet.getPrevious(nanosAfterEpochInf(time))
        resultSet.invert().getAt(nanosAfterEpochInf(time)) == inputSet.getAt(nanosAfterEpochInf(time))
        resultSet.invert().getNext(nanosAfterEpochInf(time)) == inputSet.getNext(nanosAfterEpochInf(time))
        resultSet.getPrevious(nanosAfterEpochInf(time)).map(res -> previousPresent && res == TimeSpan.withBounds(nanosAfterEpochInf(previousStart), nanosAfterEpochInf(previousEnd))).orElse(!previousPresent)
        resultSet.getAt(nanosAfterEpochInf(time)).map(res -> atPresent && res == TimeSpan.withBounds(nanosAfterEpochInf(atStart), nanosAfterEpochInf(atEnd))).orElse(!atPresent)
        resultSet.getNext(nanosAfterEpochInf(time)).map(res -> nextPresent && res == TimeSpan.withBounds(nanosAfterEpochInf(nextStart), nanosAfterEpochInf(nextEnd))).orElse(!nextPresent)

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
        0     | 10    | MAX   || true  | 10    | MAX   | false | 0     | 0     | false | 0     | 0
        9     | 10    | MIN   || false | 0     | 0     | true  | MIN   | 9     | true  | 10    | MAX
        9     | 10    | MIN1  || false | 0     | 0     | true  | MIN   | 9     | true  | 10    | MAX
        9     | 10    | -10   || false | 0     | 0     | true  | MIN   | 9     | true  | 10    | MAX
        9     | 10    | 0     || false | 0     | 0     | true  | MIN   | 9     | true  | 10    | MAX
        9     | 10    | 9     || true  | MIN   | 9     | false | 0     | 0     | true  | 10    | MAX
        9     | 10    | 10    || true  | MIN   | 9     | true  | 10    | MAX   | false | 0     | 0
        9     | 10    | 11    || true  | MIN   | 9     | true  | 10    | MAX   | false | 0     | 0
        9     | 10    | MAX1  || true  | MIN   | 9     | true  | 10    | MAX   | false | 0     | 0
        9     | 10    | MAX   || true  | 10    | MAX   | false | 0     | 0     | false | 0     | 0
    }

    private static class DefaultsTimeSpan implements CountableTimeSet {
        private final TimeSpan delegate

        DefaultsTimeSpan(TimeSpan delegate) {
            this.delegate = delegate
        }

        @Override
        Optional<TimeSpan> getPrevious(@NotNull InfInstant time) throws TimeSetException.Unchecked {
            return this.delegate.getPrevious(time)
        }

        @Override
        Optional<TimeSpan> getAt(@NotNull InfInstant time) throws TimeSetException.Unchecked {
            return this.delegate.getAt(time)
        }

        @Override
        Optional<TimeSpan> getNext(@NotNull InfInstant time) throws TimeSetException.Unchecked {
            return this.delegate.getNext(time)
        }

        @NotNull
        @Override
        TimeSet accept(TimeSetBinaryOperationVisitor visitor, TimeSet other) {
            return this.delegate.accept(visitor, other)
        }

        @NotNull
        @Override
        TimeSet accept(TimeSetUnaryOperationVisitor visitor) {
            return this.delegate.accept(visitor)
        }

        @Override
        boolean isEmpty() {
            return false
        }

        @Override
        String toString() {
            return delegate.toString()
        }
    }

    def "invert FiniteTimeSet"() {
        given:
        def inputSet = new TimeSpanSet();

        when:
        inputSet.add(TimeSpan.withBounds(nanosAfterEpochInf(aStart), nanosAfterEpochInf(aEnd)))
        inputSet.add(TimeSpan.withBounds(nanosAfterEpochInf(bStart), nanosAfterEpochInf(bEnd)))
        def resultSet = inputSet.invert()

        then:
        resultSet.invert().getPrevious(nanosAfterEpochInf(time)) == inputSet.getPrevious(nanosAfterEpochInf(time))
        resultSet.invert().getAt(nanosAfterEpochInf(time)) == inputSet.getAt(nanosAfterEpochInf(time))
        resultSet.invert().getNext(nanosAfterEpochInf(time)) == inputSet.getNext(nanosAfterEpochInf(time))
        resultSet.getPrevious(nanosAfterEpochInf(time)).map(res -> previousPresent && res == TimeSpan.withBounds(nanosAfterEpochInf(previousStart), nanosAfterEpochInf(previousEnd))).orElse(!previousPresent)
        resultSet.getAt(nanosAfterEpochInf(time)).map(res -> atPresent && res == TimeSpan.withBounds(nanosAfterEpochInf(atStart), nanosAfterEpochInf(atEnd))).orElse(!atPresent)
        resultSet.getNext(nanosAfterEpochInf(time)).map(res -> nextPresent && res == TimeSpan.withBounds(nanosAfterEpochInf(nextStart), nanosAfterEpochInf(nextEnd))).orElse(!nextPresent)

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
        -5    | 5     | 6     | 10    | MAX   || true  | 10    | MAX   | false | 0     | 0     | false | 0     | 0
    }
}
