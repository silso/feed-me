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

	@Override
	public Optional<TimeSpan> getPrevious(@NotNull Instant time) throws TimeSetException.Unchecked {
		Instant testTime = sets
			.stream()
			.map(set -> set.getPrevious(time))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(TimeSpan::end)
			.reduce(TimeUtils::latest)
			.map(InfInstant::getInstantOrElseThrow)
			.orElse(null);
		if (testTime == null) {
			return Optional.empty();
		}
		for (int i = 0; i < ITERATION_LIMIT; i++) {
			boolean found = true;
			InfInstant latestStart = InfInstant.infinitePast();
			// Check if every set has a span that contains (end inclusive) the test time
			for (final TimeSet set : sets) {
				if (!(set.getPreviousInclusive(testTime).orElse(null) instanceof TimeSpan span)) {
					return Optional.empty();
				}
				// Unless that span starts at the test time
				if (!span.containsEndInclusive(testTime) || span.start().equals(InfInstant.of(testTime))) {
					found = false;
					break;
				}
				latestStart = TimeUtils.latest(latestStart, span.start());
			}
			// If that is the case, we return a time span with the latest start as the start, and the test time as the end
			if (found) {
				assert latestStart != null;
				return Optional.of(TimeSpan.withBounds(latestStart, InfInstant.of(testTime)));
			}
			// Otherwise we find the closest previous end time and repeat
			InfInstant latestEnd = InfInstant.infinitePast();
			for (final TimeSet set : sets) {
				if (set.getPreviousEndInclusive(testTime).map(TimeSpan::end).orElse(null) instanceof InfInstant end) {
					// Pretty sure we can skip earlier than this but I can't think it through right now
					latestEnd = TimeUtils.latest(latestEnd, end);
				}
			}
			testTime = latestEnd.getInstantOrElseThrow();
		}
		throw new TimeSetException("Failed to find time set in iteration limit").unchecked();
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
		Instant testTime = sets
			.stream()
			.map(set -> set.getNext(time))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(TimeSpan::start)
			.reduce(TimeUtils::earliest)
			.map(InfInstant::getInstantOrElseThrow)
			.orElse(null);
		if (testTime == null) {
			return Optional.empty();
		}
		for (int i = 0; i < ITERATION_LIMIT; i++) {
			boolean found = true;
			InfInstant earliestEnd = InfInstant.infiniteFuture();
			// Check if every set has a span that contains (end inclusive) the test time
			for (final TimeSet set : sets) {
				if (!(set.getNextInclusive(testTime).orElse(null) instanceof TimeSpan span)) {
					return Optional.empty();
				}
				// Unless that span starts at the test time
				if (!span.contains(testTime)) {
					found = false;
					break;
				}
				earliestEnd = TimeUtils.earliest(earliestEnd, span.end());
			}
			// If that is the case, we return a time span with the latest start as the start, and the test time as the end
			if (found) {
				assert earliestEnd != null;
				return Optional.of(TimeSpan.withBounds(InfInstant.of(testTime), earliestEnd));
			}
			// Otherwise we find the closest previous end time and repeat
			InfInstant earliestStart = InfInstant.infiniteFuture();
			for (final TimeSet set : sets) {
				if (set.getNext(testTime).map(TimeSpan::start).orElse(null) instanceof InfInstant start) {
					// Pretty sure we can skip earlier than this but I can't think it through right now
					earliestStart = TimeUtils.earliest(earliestStart, start);
				}
			}
			testTime = earliestStart.getInstantOrElseThrow();
		}
		throw new TimeSetException("Failed to find time span in iteration limit").unchecked();
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
