package feedme.domain.schedule.timeset;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.TimeZone;

public abstract class DiscretePeriodicTimeSet<DiscreteTime extends DiscretePeriodicTimeSet.DiscreteInstant<DiscreteTime>> implements PeriodicTimeSet {

    @Override
    public Optional<TimeSpan> getPrevious(Instant time) throws TimeSetException.Unchecked {
        return Optional.empty();
    }

    @Override
    public Optional<TimeSpan> getAt(Instant time) throws TimeSetException.Unchecked {
        DiscreteTime discreteTime = toDiscrete(time);
        long stepsUntil = stepsUntil(discreteTime);
        if (stepsUntil <= 0) {
            DiscreteTime endTime = discreteTime.plus(-stepsUntil);
            long stepsSince = stepsSince(discreteTime);
            if (stepsSince > 0) {
                throw new TimeSetException("CountdownFunction until and since contradict, until is non-positive, since is positive").unchecked();
            }
            DiscreteTime startTime = discreteTime.minus(-stepsSince);
            return Optional.of(TimeSpan.ofInstants(startTime.getStart(), endTime.getEnd()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<TimeSpan> getNext(Instant time) throws TimeSetException.Unchecked {
        DiscreteTime discreteTime = toDiscrete(time);
        DiscreteTime startTime;
        DiscreteTime endTime;
        long stepsUntil = stepsUntil(discreteTime);
        if (stepsUntil <= 0) {
            DiscreteTime timeToCheckFrom = discreteTime.plus(-stepsUntil).plus(1);
            startTime = timeToCheckFrom.plus(stepsUntil(timeToCheckFrom));
        } else {
            startTime = discreteTime.plus(stepsUntil);
        }

        endTime = startTime.plus(-stepsUntil(startTime));

        return Optional.of(TimeSpan.ofInstants(startTime.getStart(), endTime.getEnd()));
    }

    protected abstract DiscreteTime toDiscrete(Instant continuousTime);
    protected abstract long stepsSince(DiscreteTime time);
    protected abstract long stepsUntil(DiscreteTime time);

    protected abstract static class DiscreteInstant<DiscreteTime extends DiscreteInstant<DiscreteTime>> {
        protected abstract Instant getStart();

        protected abstract Instant getEnd();

        protected abstract DiscreteTime plus(long stepsToAdd);

        protected abstract DiscreteTime minus(long stepsToSubtract);
    }

    protected static class DayInstant extends DiscreteInstant<DayInstant> {

        private final TimeZone timeZone;
        private final LocalDate localDate;

        protected DayInstant(TimeZone timeZone, Instant time) {
            this(timeZone, time.atZone(timeZone.toZoneId()).toLocalDate());
        }

        protected DayInstant(TimeZone timeZone, LocalDate localDate) {
            this.timeZone = timeZone;
            this.localDate = localDate;
        }

        @Override
        public Instant getStart() {
            return localDate.atStartOfDay().atZone(timeZone.toZoneId()).toInstant();
        }

        @Override
        public Instant getEnd() {
            return localDate.plusDays(1).atStartOfDay().atZone(timeZone.toZoneId()).toInstant().minusNanos(1);
        }

        @Override
        public DayInstant plus(long stepsToAdd) {
            return new DayInstant(timeZone, localDate.plusDays(stepsToAdd));
        }

        @Override
        public DayInstant minus(long stepsToSubtract) {
            return new DayInstant(timeZone, localDate.minusDays(stepsToSubtract));
        }

        public DayOfWeek getDayOfWeek() {
            return localDate.getDayOfWeek();
        }
    }
}
