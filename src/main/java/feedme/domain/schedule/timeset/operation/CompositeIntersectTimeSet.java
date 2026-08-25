package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import feedme.util.InfInstant;
import feedme.util.TimeUtils;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class CompositeIntersectTimeSet extends CompositeTimeSet {
	CompositeIntersectTimeSet(List<TimeSet> sets) {
		super(sets);
	}

	// Create visibility for this in this package
	@Override
	protected @NotNull List<TimeSet> getSets() {
		return super.getSets();
	}

	// This is uglier than the other one since you can't use getAt for the end time.
	// TODO: make this look nicer
	@Override
	public Optional<TimeSpan> getPrevious(@NotNull Instant time) throws TimeSetException.Unchecked {
		Instant testTime = null;
		// TODO: set limit
		while (true) {
			testTime = Optional.ofNullable(testTime).orElse(time);
			final Instant testTime0 = testTime;
			List<TimeSpan> allPrevious = sets
				.stream()
				.map(set -> set.getPrevious(testTime0))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
			if (allPrevious.size() != sets.size()) {
				return Optional.empty();
			}
			InfInstant latestStart = InfInstant.infiniteFuture();
			InfInstant earliestEnd = InfInstant.infinitePast();
			for (TimeSpan timeSpan : allPrevious) {
				latestStart = TimeUtils.latest(latestStart, timeSpan.start());
				earliestEnd = TimeUtils.earliest(earliestEnd, timeSpan.end());
			}
			if (latestStart.isInfiniteFuture()) {
				return Optional.empty();
			}
			if (latestStart.isInfinitePast()) {
				return Optional.of(TimeSpan.withBounds(latestStart, earliestEnd));
			}
			final Instant latestStartInstant = latestStart.getInstantOrElseThrow();
			// TODO: memoize getPrevious, getAt, getNext anyways?
			if (sets.stream().allMatch(set -> set.contains(latestStartInstant))) {
				return getAt(latestStartInstant);
			}
			testTime = latestStartInstant;
		}
	}

	@Override
	public Optional<TimeSpan> getAt(@NotNull Instant time) throws TimeSetException.Unchecked {
		@Nullable InfInstant start = null;
		@Nullable InfInstant end = null;
		for (TimeSet maybeSet : sets) {
			if (!(maybeSet.getAt(time).orElse(null) instanceof TimeSpan span)) {
				return Optional.empty();
			}
			if (start == null) {
				start = span.start();
				end = span.end();
				continue;
			}
			start = TimeUtils.latest(start, span.start());
			end = TimeUtils.earliest(end, span.end());
		}
		assert start != null;
		return Optional.of(TimeSpan.withBounds(start, end));
	}

	@Override
	public Optional<TimeSpan> getNext(@NotNull Instant time) throws TimeSetException.Unchecked {
		Instant testTime = null;
		// TODO: set limit
		while (true) {
			testTime = Optional.ofNullable(testTime).orElse(time);
			final Instant testTime0 = testTime;
			if (!(sets
				.stream()
				.map(set -> set.getNext(testTime0))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(TimeSpan::start)
				.reduce(TimeUtils::latest)
				.map(InfInstant::getInstantOrElseThrow)
				.orElse(null) instanceof Instant latestStart
			)) {
				return Optional.empty();
			}
			// TODO: inefficient, this iterates over sets like 3 times
			// TODO: memoize getPrevious, getAt, getNext anyways?
			if (sets.stream().allMatch(set -> set.contains(latestStart))) {
				return getAt(latestStart);
			}
			testTime = latestStart;
		}
	}

	@Override
	public @NotNull TimeSet accept(TimeSetBinaryOperationVisitor visitor, TimeSet other) {
		return visitor.visit(other, this);
	}

	@Override
	public @NotNull TimeSet accept(TimeSetUnaryOperationVisitor visitor) {
		return visitor.visit(this);
	}

	// TODO: add as TimeSet default?
	@Override
	public boolean isEmpty() {
		Instant time = Instant.EPOCH;
		return !contains(time) && getNext(time).isEmpty() && getPrevious(time).isEmpty();
	}
}
