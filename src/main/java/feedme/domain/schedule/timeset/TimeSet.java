package feedme.domain.schedule.timeset;

import feedme.domain.schedule.timeset.operation.*;
import feedme.util.InfInstant;
import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

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
    Optional<TimeSpan> getPrevious(@NotNull Instant time) throws TimeSetException.Unchecked;

    /**
     * Try to find the time span in this set that includes the given time.
     * @param time the given time to check for.
     * @return the time span in this set that contains the given time, otherwise empty.
     * @throws TimeSetException.Unchecked if a problem occurs during this search (possible with the results of set operations)
     */
    Optional<TimeSpan> getAt(@NotNull Instant time) throws TimeSetException.Unchecked;

    /**
     * Try to find the closest time span that is exclusively after the given time, i.e. the earliest time span in this
     * set that is after and does not include the given time.
     *
     * @param time the given time that is before the returned time span (if it exists).
     * @return the latest time span in this set before this time if it exists, otherwise empty.
     * @throws TimeSetException.Unchecked if a problem occurs during this search (possible with the results of set operations)
     */

    Optional<TimeSpan> getNext(@NotNull Instant time) throws TimeSetException.Unchecked;

    @NotNull TimeSet accept(TimeSetBinaryOperationVisitor visitor, TimeSet other);

    @NotNull TimeSet accept(TimeSetUnaryOperationVisitor visitor);

    // TODO reevaluate if this is needed, implementing is not necessarily simple
    boolean isEmpty();

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

    default boolean contains(Instant time) {
        return getAt(time).isPresent();
    }

    // TODO: create a clearer API for switching start and end inclusivity and exclusivity

    default Optional<TimeSpan> getPreviousEndInclusive(Instant time) throws TimeSetException.Unchecked {
        return getPrevious(time).flatMap(span -> {
            if (span.end().equals(InfInstant.of(time))) {
                if (span.start().isInfinitePast()) {
                    return Optional.empty();
                } else {
                    return getPrevious(span.start().getInstantOrElseThrow());
                }
            } else {
                return Optional.of(span);
            }
        });
    }

    default Optional<TimeSpan> getAtEndInclusive(Instant time) throws TimeSetException.Unchecked {
        return getAt(time).or(() -> getPrevious(time).filter(span -> span.end().getInstant().orElseThrow().equals(time)));
    }

    default boolean containsEndInclusive(Instant time) {
        return getAtEndInclusive(time).isPresent();
    }

    default TimeSet unionWith(TimeSet other) {
        return accept(new UnionOperationVisitor(), other);
    }

    default TimeSet intersectWith(TimeSet other) {
        return accept(new IntersectOperationVisitor(), other);
    }

    default TimeSet invert() {
        return accept(new InvertOperationVisitor());
    }

    default TimeSet differenceWith(TimeSet other) {
        return this.intersectWith(other.invert());
    }

    static TimeSet empty() {
        return EmptyTimeSet.get();
    }

    static TimeSet everything() {
        return EverythingTimeSet.get();
    }
}
