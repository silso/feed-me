package feedme.domain.schedule.timeset;

import feedme.domain.schedule.timeset.operation.TimeSetBinaryOperationVisitor;
import feedme.domain.schedule.timeset.operation.TimeSetUnaryOperationVisitor;
import java.time.*;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class TimeOfDayTimeSet implements PeriodicTimeSet {

    private final ZoneId timeZone;
    private final ZoneRules zoneRules;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final boolean isOvernight;

    public TimeOfDayTimeSet(ZoneId timeZone, LocalTime startTime, LocalTime endTime) {
        if (startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time cannot equal end time");
        }
        this.timeZone = timeZone;
        this.zoneRules = timeZone.getRules();
        this.startTime = startTime;
        this.endTime = endTime;
        this.isOvernight = endTime.isBefore(startTime);
    }

    @Override
    public Optional<TimeSpan> getPrevious(@NotNull Instant time) throws TimeSetException.Unchecked {
        LocalDate date = time.atZone(timeZone).toLocalDate();
        Instant end = getLatestTime(endTime.atDate(date));
        if (time.isBefore(end)) {
            date = date.minusDays(1);
        }
        end = getLatestTime(endTime.atDate(date));
        if (isOvernight) {
            date = date.minusDays(1);
        }
        Instant start = getEarliestTime(startTime.atDate(date));
        return Optional.of(TimeSpan.withBounds(start, end));
    }

    @Override
    public Optional<TimeSpan> getAt(@NotNull Instant time) throws TimeSetException.Unchecked {
        LocalDate date = time.atZone(timeZone).toLocalDate();
        Instant start = getEarliestTime(startTime.atDate(date));
        if (!isOvernight) {
            Instant end = getLatestTime(endTime.atDate(date));
            TimeSpan span = TimeSpan.withBounds(start, end);
            return span.getAt(time);
        } else {
            if (time.isAfter(start) || time.equals(start)) {
                Instant end = getLatestTime(endTime.atDate(date.plusDays(1)));
                return Optional.of(TimeSpan.withBounds(start, end));
            } else {
                Instant end = getLatestTime(endTime.atDate(date));
                if (time.isBefore(end)) {
                    start = getEarliestTime(startTime.atDate(date.minusDays(1)));
                    return Optional.of(TimeSpan.withBounds(start, end));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<TimeSpan> getNext(@NotNull Instant time) throws TimeSetException.Unchecked {
        LocalDate date = time.atZone(timeZone).toLocalDate();
        Instant start = getEarliestTime(startTime.atDate(date));
        if (time.isAfter(start) || time.equals(start)) {
            date = date.plusDays(1);
        }
        start = getEarliestTime(startTime.atDate(date));
        if (isOvernight) {
            date = date.plusDays(1);
        }
        Instant end = getLatestTime(endTime.atDate(date));
        return Optional.of(TimeSpan.withBounds(start, end));
    }

    @Override
    public @NotNull TimeSet accept(TimeSetBinaryOperationVisitor visitor, TimeSet other) {
        return visitor.visit(other, this);
    }

    @Override
    public @NotNull TimeSet accept(TimeSetUnaryOperationVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public ZoneRules getZoneRules() {
        return zoneRules;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isOvernight() {
        return isOvernight;
    }

    private Instant getEarliestTime(LocalDateTime time) {
        ZoneOffsetTransition transition = zoneRules.getTransition(time);
        if (transition == null) {
            return time.toInstant(zoneRules.getOffset(time));
        } else if (transition.isGap()) {
            // using this time for both since it's the round time
            return transition.getDateTimeAfter().toInstant(transition.getOffsetAfter());
        } else {
            // overlap
            return time.toInstant(transition.getOffsetBefore());
        }
    }

    private Instant getLatestTime(LocalDateTime time) {
        ZoneOffsetTransition transition = zoneRules.getTransition(time);
        if (transition == null) {
            return time.toInstant(zoneRules.getOffset(time));
        } else if (transition.isGap()) {
            return transition.getDateTimeAfter().toInstant(transition.getOffsetAfter());
        } else {
            // overlap
            return time.toInstant(transition.getOffsetAfter());
        }
    }
}
