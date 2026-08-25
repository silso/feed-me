package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import feedme.util.InfInstant;
import feedme.util.TimeUtils;
import java.time.Instant;
import java.util.*;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class CompositeUnionTimeSet extends CompositeTimeSet {
	CompositeUnionTimeSet(List<TimeSet> sets) {
		super(sets);
	}

	// Create visibility for this in this package
	@Override
	protected @NotNull List<TimeSet> getSets() {
		return super.getSets();
	}

	@Override
	public Optional<TimeSpan> getPrevious(@NotNull Instant time) throws TimeSetException.Unchecked {
		final Instant testTime;
		if (getAt(time).orElse(null) instanceof TimeSpan currentSpan) {
			if (currentSpan.start().isInfinitePast()) {
				return Optional.empty();
			} else {
				testTime = currentSpan.start().getInstantOrElseThrow();
			}
		} else {
			testTime = time;
		}
		return sets
			.stream()
			.map(set -> set.getPrevious(testTime))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.reduce(TimeSpan::latestEndTime)
			.flatMap(span -> {
				if (span.start().isInfinitePast()) {
					return Optional.of(span);
				} else {
					return getAt(span.start().getInstantOrElseThrow());
				}
			});
	}

	@Override
	public Optional<TimeSpan> getAt(@NotNull Instant time) throws TimeSetException.Unchecked {
		@Nullable InfInstant start = null;
		@Nullable InfInstant end = null;
		@Nullable InfInstant newStart = null;
		@Nullable InfInstant newEnd = null;
		// TODO: set limit
		do {
			start = newStart;
			end = newEnd;
			if (start == null || !start.isInfinitePast()) {
				Instant startTestTime = Optional.ofNullable(start).map(InfInstant::getInstantOrElseThrow).orElse(time);
				newStart =
					sets
						.stream()
						.map(set -> set.getContiguous(startTestTime))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.map(TimeSpan::start)
						.reduce(TimeUtils::earliest)
						.orElse(null);
			}
			if (end == null || !end.isInfiniteFuture()) {
				Instant endTestTime = Optional.ofNullable(end).map(InfInstant::getInstantOrElseThrow).orElse(time);
				newEnd =
					sets
						.stream()
						.map(set -> set.getContiguous(endTestTime))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.map(TimeSpan::end)
						.reduce(TimeUtils::latest).orElse(null);
			}
		} while (!Objects.equals(start, newStart) || !Objects.equals(end, newEnd));
		if (start == null || end == null) {
			return Optional.empty();
		}
		return Optional.of(TimeSpan.withBounds(start, end));
	}

	@Override
	public Optional<TimeSpan> getNext(@NotNull Instant time) throws TimeSetException.Unchecked {
		final Instant testTime;
		if (getAt(time).orElse(null) instanceof TimeSpan currentSpan) {
			if (currentSpan.end().isInfiniteFuture()) {
				return Optional.empty();
			} else {
				testTime = currentSpan.end().getInstantOrElseThrow();
			}
		} else {
			testTime = time;
		}
		return sets
			.stream()
			.map(set -> set.getNext(testTime))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(TimeSpan::start)
			.reduce(TimeUtils::earliest)
			.map(InfInstant::getInstantOrElseThrow)
			.flatMap(this::getAt);
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
		return sets.stream().allMatch(TimeSet::isEmpty);
	}
}
