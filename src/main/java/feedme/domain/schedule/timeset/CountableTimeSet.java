package feedme.domain.schedule.timeset;

import com.google.common.collect.Streams;
import feedme.util.InfInstant;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface CountableTimeSet extends TimeSet {

	Optional<TimeSpan> getPrevious(InfInstant time) throws TimeSetException.Unchecked;
	Optional<TimeSpan> getAt(InfInstant time) throws TimeSetException.Unchecked;
	Optional<TimeSpan> getNext(InfInstant time) throws TimeSetException.Unchecked;

	@Override
	default Optional<TimeSpan> getPrevious(@NotNull Instant time) throws TimeSetException.Unchecked {
		return getPrevious(InfInstant.of(time));
	}

	@Override
	default Optional<TimeSpan> getAt(@NotNull Instant time) throws TimeSetException.Unchecked {
		return getAt(InfInstant.of(time));
	}

	@Override
	default Optional<TimeSpan> getNext(@NotNull Instant time) throws TimeSetException.Unchecked {
		return getNext(InfInstant.of(time));
	}

	/**
	 * Like {@link #getPrevious}, but could include the given time
	 * @param time the given time.
	 * @return the returned time span
	 * @throws TimeSetException.Unchecked if a problem occurs during this search (possible with the results of set operations)
	 */
	default Optional<TimeSpan> getPreviousInclusive(InfInstant time) throws TimeSetException.Unchecked {
		return getAt(time).or(() -> getPrevious(time));
	}

	/**
	 * Like {@link #getNext}, but could include the given time
	 * @param time the given time.
	 * @return the returned time span
	 * @throws TimeSetException.Unchecked if a problem occurs during this search (possible with the results of set operations)
	 */
	default Optional<TimeSpan> getNextInclusive(InfInstant time) throws TimeSetException.Unchecked {
		return getAt(time).or(() -> getNext(time));
	}

	default boolean contains(InfInstant time) {
		return getAt(time).isPresent();
	}

	default Optional<TimeSpan> getFirst() {
		return getNextInclusive(InfInstant.infinitePast());
	}

	default Optional<TimeSpan> getLast() {
		return getPreviousInclusive(InfInstant.infiniteFuture());
	}

	default boolean isEmpty() {
		return getFirst().isEmpty();
	}

	default Stream<TimeSpan> streamForwardFrom(InfInstant time) {
		if (isEmpty()) {
			return Stream.empty();
		}
		return Stream.iterate(
				time,
				(InfInstant currentTime) -> getNext(currentTime).isPresent(),
				(InfInstant currentTime) -> getNext(currentTime).orElseThrow().start()
			).map(this::getAt)
			.flatMap(Optional::stream);
	}

	default Stream<TimeSpan> streamBackwardFrom(InfInstant time) {
		if (isEmpty()) {
			return Stream.empty();
		}
		return Stream.iterate(
				time,
				(InfInstant currentTime) -> getPrevious(currentTime).isPresent(),
				(InfInstant currentTime) -> getPrevious(currentTime).orElseThrow().start()
			).map(this::getAt)
			.map(Optional::orElseThrow);
	}

	default Stream<TimeSpan> streamForward() {
		if (isEmpty()) {
			return Stream.empty();
		}
		if (getFirst().get().start().isInfinitePast()) {
			return Streams.concat(Stream.of(getFirst().get()), streamForwardFrom(InfInstant.infinitePast()));
		}
		return streamForwardFrom(InfInstant.infinitePast());
	}

	default Stream<TimeSpan> streamBackward() {
		if (isEmpty()) {
			return Stream.empty();
		}
		if (getLast().get().end().isInfiniteFuture()) {
			return Streams.concat(Stream.of(getLast().get()), streamBackwardFrom(InfInstant.infiniteFuture()));
		}
		return streamBackwardFrom(InfInstant.infiniteFuture());
	}

	default Iterable<TimeSpan> iterateForward() {
		return new Iterable<>() {
			@Override
			public @NotNull Iterator<TimeSpan> iterator() {
				return streamForward().iterator();
			}
		};
	}

	default Iterable<TimeSpan> iterateBackward() {
		return new Iterable<>() {
			@Override
			public @NotNull Iterator<TimeSpan> iterator() {
				return streamBackward().iterator();
			}
		};
	}
}
