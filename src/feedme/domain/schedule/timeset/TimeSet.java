package feedme.domain.schedule.timeset;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Important interface for scheduling, this represents any imaginable set of {@link Instant}s in time. TimeSets are
 * queried by checking whether a time is contained ({@link #contains}) or by searching for a {@link TimeSpan} at
 * ({@link #getAt}), before ({@link #getPrevious}), or after ({@link #getNext}) an instant in time. TimeSets also
 * provide the ability to perform set operations with other TimeSets to construct more complicated ones.
 */
public interface TimeSet {
    /**
     * Try to find the closest time span that is exclusively before the given time, i.e. the latest time span in this
     * set that is before and does not include the given time.
     *
     * @param time the given time that is after the returned time span (if it exists).
     * @return the latest time span in this set before this time if it exists, otherwise empty.
     * @throws TimeSetException.Unchecked if a problem occurs during this search (possible with the results of set operations)
     */
    Optional<TimeSpan> getPrevious(Instant time) throws TimeSetException.Unchecked;

    /**
     * Try to find the time span in this set that includes the given time.
     * @param time the given time to check for.
     * @return the time span in this set that contains the given time, otherwise empty.
     * @throws TimeSetException.Unchecked if a problem occurs during this search (possible with the results of set operations)
     */
    Optional<TimeSpan> getAt(Instant time) throws TimeSetException.Unchecked;

    /**
     * Try to find the closest time span that is exclusively after the given time, i.e. the earliest time span in this
     * set that is after and does not include the given time.
     *
     * @param time the given time that is before the returned time span (if it exists).
     * @return the latest time span in this set before this time if it exists, otherwise empty.
     * @throws TimeSetException.Unchecked if a problem occurs during this search (possible with the results of set operations)
     */
    Optional<TimeSpan> getNext(Instant time) throws TimeSetException.Unchecked;

    /**
     * Like {@link #getPrevious}, but could include the given time
     * @param time the given time.
     * @return the returned time span
     * @throws TimeSetException.Unchecked if a problem occurs during this search (possible with the results of set operations)
     */
    default Optional<TimeSpan> getPreviousInclusive(Instant time) throws TimeSetException.Unchecked {
        return getAt(time).or(() -> getPrevious(time));
    }

    /**
     * Like {@link #getNext}, but could include the given time
     * @param time the given time.
     * @return the returned time span
     * @throws TimeSetException.Unchecked if a problem occurs during this search (possible with the results of set operations)
     */
    default Optional<TimeSpan> getNextInclusive(Instant time) throws TimeSetException.Unchecked {
        return getAt(time).or(() -> getNext(time));
    }

    default Optional<TimeSpan> getPreviousContiguousWith(TimeSpan other) throws TimeSetException.Unchecked {
        return getPreviousInclusive(other.startTime()).filter(span -> !span.endTime().isBefore(other.startTime()));
    }

    default Optional<TimeSpan> getNextContiguousWith(TimeSpan other) throws TimeSetException.Unchecked {
        return getNextInclusive(other.endTime()).filter(span -> !span.startTime().isAfter(other.endTime()));
    }

    default TimeSet unionWith(TimeSet other) {
        TimeSet[] sets = new TimeSet[]{this, other};
        return new TimeSet() {
            @Override
            public Optional<TimeSpan> getPrevious(Instant time) {
                try {
                    final Instant startSearchTime;
                    if (Arrays.stream(sets).anyMatch(set -> set.contains(time))) {
                        startSearchTime = TimeSetUtils.findPreviousEarliestContainedTime(time, sets).minusNanos(1);
                    } else {
                        startSearchTime = time;
                    }
                    Optional<Instant> endTimeMaybe = Stream
                        .of(sets)
                        .map(set -> set.getPreviousInclusive(startSearchTime))
                        .flatMap(Optional::stream)
                        .min(Comparator.comparing(TimeSpan::startTime))
                        .map(TimeSpan::startTime);
                    if (endTimeMaybe.isEmpty()) {
                        return Optional.empty();
                    }
                    Instant startTime = TimeSetUtils.findPreviousEarliestContainedTime(endTimeMaybe.get(), sets);
                    return Optional.of(TimeSpan.ofInstants(startTime, endTimeMaybe.get()));
                } catch (TimeSetException e) {
                    return Optional.empty();
                }
            }

            @Override
            public Optional<TimeSpan> getAt(Instant time) {
                try {
                    Instant startTime = TimeSetUtils.findPreviousEarliestContainedTime(time, sets);
                    Instant endTime = TimeSetUtils.findNextEmptyTime(time, sets);
                    if (startTime.equals(time) || endTime.equals(time)) {
                        return Optional.empty();
                    } else {
                        return Optional.of(TimeSpan.ofInstants(startTime, endTime));
                    }
                } catch (TimeSetException e) {
                    return Optional.empty();
                }
            }

            @Override
            public Optional<TimeSpan> getNext(Instant time) {
                try {
                    final Instant startSearchTime;
                    if (Arrays.stream(sets).anyMatch(set -> set.contains(time))) {
                        startSearchTime = TimeSetUtils.findNextEmptyTime(time, sets);
                    } else {
                        startSearchTime = time;
                    }
                    Optional<Instant> startTimeMaybe = Stream
                        .of(sets)
                        .map(set -> set.getNextInclusive(startSearchTime))
                        .flatMap(Optional::stream)
                        .min(Comparator.comparing(TimeSpan::startTime))
                        .map(TimeSpan::startTime);
                    if (startTimeMaybe.isEmpty()) {
                        return Optional.empty();
                    }
                    Instant endTime = TimeSetUtils.findNextEmptyTime(startTimeMaybe.get(), sets);
                    return Optional.of(TimeSpan.ofInstants(startTimeMaybe.get(), endTime));
                } catch (TimeSetException e) {
                    return Optional.empty();
                }
            }
        };
    }

    default TimeSet unionWithTimeSpan(TimeSpan other) {
        // TODO: actual implementation
        return unionWith(other);
    }

    default TimeSet intersectWith(TimeSet other) {
        if (other instanceof TimeSpan span) {
            return this.intersectWithTimeSpan(span);
        }
        return this.invert().unionWith(other.invert()).invert();
    }

    default MeasurableTimeSet intersectWithTimeSpan(TimeSpan other) {
        TimeSet thisSet = this;
        return new MeasurableTimeSet() {
            @Override
            public Optional<TimeSpan> getPrevious(Instant time) {
                if (other.getAt(time).isPresent()) {
                    return thisSet.getPrevious(time).map(other::intersectWithTimeSpan).flatMap(set -> set.getPrevious(time));
                } else if (other.getPrevious(time).isPresent()) {
                    return thisSet.getPreviousInclusive(other.lastTime()).map(other::intersectWithTimeSpan).flatMap(set -> set.getPrevious(time));
                } else {
                    return Optional.empty();
                }
            }

            @Override
            public Optional<TimeSpan> getAt(Instant time) {
                return thisSet.getAt(time).map(other::intersectWithTimeSpan).flatMap(set -> set.getAt(time));
            }

            @Override
            public Optional<TimeSpan> getNext(Instant time) {
                if (other.getAt(time).isPresent()) {
                    return thisSet.getNext(time).map(other::intersectWithTimeSpan).flatMap(set -> set.getNext(time));
                } else if (other.getNext(time).isPresent()) {
                    return thisSet.getNextInclusive(other.startTime()).map(other::intersectWithTimeSpan).flatMap(set -> set.getNext(time));
                } else {
                    return Optional.empty();
                }
            }
        };
    }

    default TimeSet differenceWith(TimeSet other) {
        return this.intersectWith(other.invert());
    }

    default boolean contains(Instant time) {
        return getAt(time).isPresent();
    }

    default Stream<TimeSpan> streamForwardFrom(Instant time) {
        return Stream.iterate(
                time,
                (Instant currentTime) -> getNext(currentTime).isPresent(),
                (Instant currentTime) -> getNext(currentTime).orElseThrow().startTime()
            ).map(this::getAt)
            .map(Optional::orElseThrow);
    }

    default Stream<TimeSpan> streamBackwardFrom(Instant time) {
        return Stream.iterate(
                time,
                (Instant currentTime) -> getPrevious(currentTime).isPresent(),
                (Instant currentTime) -> getPrevious(currentTime).orElseThrow().startTime()
            ).map(this::getAt)
            .map(Optional::orElseThrow);
    }

    default TimeSet invert() {
        TimeSet thisSet = this;
        return new TimeSet() {
            @Override
            public Optional<TimeSpan> getPrevious(Instant time) throws TimeSetException.Unchecked {
                return thisSet
                    .getPreviousInclusive(time)
                    .map(TimeSpan::startTime)
                    .filter(startTime -> !Instant.MIN.equals(startTime))
                    .map(endTime ->
                        TimeSpan.ofInstants(thisSet.getPrevious(endTime).map(TimeSpan::endTime).orElse(Instant.MIN), endTime)
                    );
            }

            @Override
            public Optional<TimeSpan> getAt(Instant time) throws TimeSetException.Unchecked {
                return thisSet.getAt(time)
                    .map(span -> Optional.<TimeSpan>empty())
                    .orElseGet(
                        () -> {
                            Instant startTime = thisSet.getPrevious(time).map(TimeSpan::endTime).orElse(Instant.MIN);
                            Instant endTime = thisSet.getNext(time).map(TimeSpan::startTime).orElse(Instant.MAX);
                            return Optional.of(TimeSpan.ofInstants(startTime, endTime));
                        }
                    );
            }

            @Override
            public Optional<TimeSpan> getNext(Instant time) throws TimeSetException.Unchecked {
                return thisSet
                    .getNextInclusive(time)
                    .map(TimeSpan::endTime)
                    .filter(endTime -> !Instant.MAX.equals(endTime))
                    .map(startTime ->
                        TimeSpan.ofInstants(startTime, thisSet.getNext(startTime).map(TimeSpan::startTime).orElse(Instant.MAX))
                    );
            }
        };
    }

    MeasurableTimeSet EMPTY = new MeasurableTimeSet() {
        @Override
        public Optional<TimeSpan> getPrevious(Instant time) {
            return Optional.empty();
        }

        @Override
        public Optional<TimeSpan> getAt(Instant time) {
            return Optional.empty();
        }

        @Override
        public Optional<TimeSpan> getNext(Instant time) {
            return Optional.empty();
        }

        @Override
        public TimeSet unionWith(TimeSet other) {
            return other;
        }

        @Override
        public MeasurableTimeSet unionWith(MeasurableTimeSet other) {
            return other;
        }

        @Override
        public TimeSpan unionWithTimeSpan(TimeSpan other) {
            return other;
        }

        @Override
        public MeasurableTimeSet intersectWith(TimeSet other) {
            return TimeSet.EMPTY;
        }

        @Override
        public MeasurableTimeSet intersectWithTimeSpan(TimeSpan other) {
            return TimeSet.EMPTY;
        }

        @Override
        public Duration getDuration() {
            return Duration.ZERO;
        }

        @Override
        public TimeSet invert() {
            return TimeSet.EVERYTHING;
        }
    };

    TimeSet EVERYTHING = new TimeSet() {
        @Override
        public Optional<TimeSpan> getPrevious(Instant time) throws TimeSetException.Unchecked {
            return Optional.empty();
        }

        @Override
        public Optional<TimeSpan> getAt(Instant time) throws TimeSetException.Unchecked {
            return Optional.of(new TimeSpan(Instant.MIN, Instant.MAX));
        }

        @Override
        public Optional<TimeSpan> getNext(Instant time) throws TimeSetException.Unchecked {
            return Optional.empty();
        }

        @Override
        public TimeSet invert() {
            return TimeSet.EMPTY;
        }
    };
}
