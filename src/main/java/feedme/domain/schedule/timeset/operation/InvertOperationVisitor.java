package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import feedme.util.InfInstant;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class InvertOperationVisitor implements TimeSetUnaryOperationVisitor {
	@Override
	public @NotNull TimeSet visit(TimeSpan set) {
		if (set.start().isInfinitePast()) {
			return TimeSpan.withBounds(set.end(), InfInstant.infiniteFuture());
		} else if (set.end().isInfiniteFuture()) {
			return TimeSpan.withBounds(InfInstant.infinitePast(), set.start());
		} else {
			MutableTimeSet newSet = MutableTimeSet.create();
			newSet.add(TimeSpan.withBounds(InfInstant.infinitePast(), set.start()));
			newSet.add(TimeSpan.withBounds(set.end(), InfInstant.infiniteFuture()));
			return newSet;
		}
	}

	@Override
	public @NotNull TimeSet visit(TimeSpanSet set) {
		if (set.isEmpty()) {
			return EverythingTimeSet.get();
		}
		if (set.size() == 1) {
			return visit(set.getFirst().get());
		}
		MutableTimeSet newSet = MutableTimeSet.create();
		Iterator<TimeSpan> spans = set.iterateForward().iterator();
		TimeSpan span = spans.next();
		if (!span.start().isInfinitePast()) {
			newSet.add(TimeSpan.withBounds(InfInstant.infinitePast(), span.start()));
		}
		InfInstant start = span.end();
		// There should be at least one more span
		while (spans.hasNext()) {
			span = spans.next();
			newSet.add(TimeSpan.withBounds(start, span.start()));
			start = span.end();
		}
		if (!span.end().isInfiniteFuture()) {
			newSet.add(TimeSpan.withBounds(span.end(), InfInstant.infiniteFuture()));
		}
		return newSet;
	}

	@Override
	public @NotNull TimeSet visit(CompositeUnionTimeSet set) {
		return new CompositeIntersectTimeSet(set.getSets().stream().map(TimeSet::invert).toList());
	}

	@Override
	public @NotNull TimeSet visit(CompositeIntersectTimeSet set) {
		return new CompositeUnionTimeSet(set.getSets().stream().map(TimeSet::invert).toList());
	}

	@Override
	public @NotNull TimeSet visit(DiscretePeriodicTimeSet<?> set) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public @NotNull TimeSet visit(TimeOfDayTimeSet set) {
		return new TimeOfDayTimeSet(set.getTimeZone(), set.getEndTime(), set.getStartTime());
	}

	@Override
	public @NotNull TimeSet visit(EmptyTimeSet set) {
		return EverythingTimeSet.get();
	}

	@Override
	public @NotNull TimeSet visit(EverythingTimeSet set) {
		return EmptyTimeSet.get();
	}
}
