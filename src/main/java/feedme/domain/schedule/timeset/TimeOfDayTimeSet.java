package feedme.domain.schedule.timeset;

import java.time.*;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.Optional;

public class TimeOfDayTimeSet implements PeriodicTimeSet {

    private final ZoneId timeZone;
    private final ZoneRules zoneRules;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final boolean isOvernight;

    public TimeOfDayTimeSet(ZoneId timeZone, LocalTime startTime, LocalTime endTime) {
        this.timeZone = timeZone;
        this.zoneRules = timeZone.getRules();
        this.startTime = startTime;
        this.endTime = endTime;
        this.isOvernight = endTime.isBefore(startTime);
    }

    @Override
    public Optional<TimeSpan> getPrevious(Instant time) throws TimeSetException.Unchecked {
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
        return Optional.of(TimeSpan.ofInstants(start, end));
    }

    @Override
    public Optional<TimeSpan> getAt(Instant time) throws TimeSetException.Unchecked {
        LocalDate date = time.atZone(timeZone).toLocalDate();
        Instant start = getEarliestTime(startTime.atDate(date));
        if (!isOvernight) {
            if (time.isAfter(start) || time.equals(start)) {
                Instant end = getLatestTime(endTime.atDate(date));
                return Optional.of(TimeSpan.ofInstants(start, end));
            }
        } else {
            if (time.isAfter(start) || time.equals(start)) {
                Instant end = getLatestTime(endTime.atDate(date.plusDays(1)));
                return Optional.of(TimeSpan.ofInstants(start, end));
            } else {
                Instant end = getLatestTime(endTime.atDate(date));
                if (time.isBefore(end)) {
                    start = getEarliestTime(startTime.atDate(date.minusDays(1)));
                    return Optional.of(TimeSpan.ofInstants(start, end));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<TimeSpan> getNext(Instant time) throws TimeSetException.Unchecked {
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
        return Optional.of(TimeSpan.ofInstants(start, end));
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
