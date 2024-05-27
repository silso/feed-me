package feedme.domain.schedule.timeset

import org.jetbrains.annotations.NotNull
import spock.lang.Shared
import spock.lang.Specification

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.function.Function

import static java.time.DayOfWeek.*

class DayOfWeekTimeSetTest extends Specification {
    @Shared TimeZone timeZone = TimeZone.getTimeZone("America/Chicago")

    @Shared Set<DayOfWeek> allDays = Set.of(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    @Shared Set<DayOfWeek> SUTU = Set.of(SUNDAY, TUESDAY)

    def "error days"() {
        when:
        new DayOfWeekTimeSet(timeZone, Set.of())
        then:
        thrown IllegalArgumentException
        when:
        new DayOfWeekTimeSet(timeZone, allDays)
        then:
        thrown IllegalArgumentException
    }

    def "normal days"() {
        expect:
        DayOfWeekTimeSet set = new DayOfWeekTimeSet(timeZone, days.toSet())
        LocalDate todayDay = dayAfterEpoch(today)
        Instant todayStart = todayDay.atStartOfDay(timeZone.toZoneId()).toInstant()
        Instant endOfPrevious = endOfPrevious(todayDay, prevEnd)
        set.getPrevious(todayStart).orElseThrow() == TimeSpan.ofInstants(startOfPreviousOrSame(endOfPrevious.atZone(timeZone.toZoneId()).toLocalDate(), prevStart), endOfPrevious)
        set.getAt(todayStart).map(res -> atPresent && res == TimeSpan.ofInstants(startOfPreviousOrSame(todayDay, atStart), endOfNextOrSame(todayDay, atEnd))).orElse(!atPresent)
        Instant startOfNext = startOfNext(todayDay, nextStart)
        set.getNext(todayStart).orElseThrow() == TimeSpan.ofInstants(startOfNext, endOfNextOrSame(startOfNext.atZone(timeZone.toZoneId()).toLocalDate(), nextEnd))

        where:
        days               | today            || prevStart         | prevEnd           | atPresent         | atStart           | atEnd             | nextStart         | nextEnd
        [MONDAY]           | MONDAY           || MONDAY            | MONDAY            | true              | MONDAY            | MONDAY            | MONDAY            | MONDAY
        [MONDAY]           | SATURDAY         || MONDAY            | MONDAY            | false             | MONDAY            | MONDAY            | MONDAY            | MONDAY
        [MONDAY]           | SUNDAY           || MONDAY            | MONDAY            | false             | MONDAY            | MONDAY            | MONDAY            | MONDAY
        [SUNDAY]           | MONDAY           || SUNDAY            | SUNDAY            | false             | SUNDAY            | SUNDAY            | SUNDAY            | SUNDAY
        [SUNDAY]           | SATURDAY         || SUNDAY            | SUNDAY            | false             | SUNDAY            | SUNDAY            | SUNDAY            | SUNDAY
        [SUNDAY]           | SUNDAY           || SUNDAY            | SUNDAY            | true              | SUNDAY            | SUNDAY            | SUNDAY            | SUNDAY
        [SATURDAY]         | MONDAY           || SATURDAY          | SATURDAY          | false             | SATURDAY          | SATURDAY          | SATURDAY          | SATURDAY
        [SATURDAY]         | SATURDAY         || SATURDAY          | SATURDAY          | true              | SATURDAY          | SATURDAY          | SATURDAY          | SATURDAY
        [SATURDAY]         | SUNDAY           || SATURDAY          | SATURDAY          | false             | SATURDAY          | SATURDAY          | SATURDAY          | SATURDAY
        allDays - SUNDAY   | MONDAY           || MONDAY            | SATURDAY          | true              | MONDAY            | SATURDAY          | MONDAY            | SATURDAY
        allDays - SUNDAY   | SATURDAY         || MONDAY            | SATURDAY          | true              | MONDAY            | SATURDAY          | MONDAY            | SATURDAY
        allDays - SUNDAY   | SUNDAY           || MONDAY            | SATURDAY          | false             | MONDAY            | SATURDAY          | MONDAY            | SATURDAY
        allDays - SUTU     | MONDAY           || WEDNESDAY         | SATURDAY          | true              | MONDAY            | MONDAY            | WEDNESDAY         | SATURDAY
        allDays - SUTU     | TUESDAY          || MONDAY            | MONDAY            | false             | MONDAY            | SATURDAY          | WEDNESDAY         | SATURDAY
        allDays - SUTU     | SATURDAY         || MONDAY            | MONDAY            | true              | WEDNESDAY         | SATURDAY          | MONDAY            | MONDAY
        allDays - SUTU     | SUNDAY           || WEDNESDAY         | SATURDAY          | false             | MONDAY            | SATURDAY          | MONDAY            | MONDAY
    }

    private LocalDate dayAfterEpoch(DayOfWeek day) {
        return Instant.EPOCH.atZone(timeZone.toZoneId()).toLocalDate().with(TemporalAdjusters.nextOrSame(day))
    }

    private Instant startOfPrevious(LocalDate today, DayOfWeek day) {
        return today.with(TemporalAdjusters.previous(day)).atStartOfDay(timeZone.toZoneId()).toInstant()
    }

    private Instant endOfPrevious(LocalDate today, DayOfWeek day) {
        return today.with(TemporalAdjusters.previous(day)).plusDays(1).atStartOfDay(timeZone.toZoneId()).toInstant()
    }

    private Instant startOfNext(LocalDate today, DayOfWeek day) {
        return today.with(TemporalAdjusters.next(day)).atStartOfDay(timeZone.toZoneId()).toInstant()
    }

    private Instant endOfNext(LocalDate today, DayOfWeek day) {
        return today.with(TemporalAdjusters.next(day)).plusDays(1).atStartOfDay(timeZone.toZoneId()).toInstant()
    }

    private Instant startOfPreviousOrSame(LocalDate today, DayOfWeek day) {
        return today.with(TemporalAdjusters.previousOrSame(day)).atStartOfDay(timeZone.toZoneId()).toInstant()
    }

    private Instant endOfNextOrSame(LocalDate today, DayOfWeek day) {
        return today.with(TemporalAdjusters.nextOrSame(day)).plusDays(1).atStartOfDay(timeZone.toZoneId()).toInstant()
    }
}
