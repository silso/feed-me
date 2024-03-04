package feedme.domain.schedule.timeset;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

public class WeekdaysTimeSet implements PeriodicTimeSet {
    private static final Set<DayOfWeek> WEEKDAY_SET = Set.of(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    );
    @NotNull
    private final TimeZone localTimeZone;

    public WeekdaysTimeSet(@NotNull TimeZone localTimeZone) {
        this.localTimeZone = localTimeZone;
    }

    private LocalDate getLocalDate(Instant time) {
        return LocalDateTime.ofInstant(time, localTimeZone.toZoneId()).toLocalDate();
    }

    @Override
    public Optional<TimeSpan> getPrevious(Instant time) throws TimeSetException.Unchecked {
        LocalDate localDate = getLocalDate(time);
        LocalDate localFriday = localDate.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        LocalDate localMonday = localFriday.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        Instant mondayStart = localMonday.atStartOfDay().atZone(localTimeZone.toZoneId()).toInstant();
        Instant fridayEnd = localFriday.plusDays(1).atStartOfDay().atZone(localTimeZone.toZoneId()).toInstant();
        return Optional.of(TimeSpan.ofInstants(mondayStart, fridayEnd));
    }

    @Override
    public Optional<TimeSpan> getAt(Instant time) throws TimeSetException.Unchecked {
        LocalDate localDate = getLocalDate(time);
        if (WEEKDAY_SET.contains(localDate.getDayOfWeek())) {
            LocalDate localMonday = localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate localFriday = localDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
            Instant mondayStart = localMonday.atStartOfDay().atZone(localTimeZone.toZoneId()).toInstant();
            Instant fridayEnd = localFriday.plusDays(1).atStartOfDay().atZone(localTimeZone.toZoneId()).toInstant();
            return Optional.of(TimeSpan.ofInstants(mondayStart, fridayEnd));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TimeSpan> getNext(Instant time) throws TimeSetException.Unchecked {
        LocalDate localDate = getLocalDate(time);
        LocalDate localMonday = localDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate localFriday = localMonday.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        Instant mondayStart = localMonday.atStartOfDay().atZone(localTimeZone.toZoneId()).toInstant();
        Instant fridayEnd = localFriday.plusDays(1).atStartOfDay().atZone(localTimeZone.toZoneId()).toInstant();
        return Optional.of(TimeSpan.ofInstants(mondayStart, fridayEnd));
    }
}
