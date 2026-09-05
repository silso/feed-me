package feedme.domain.schedule.timeset.operation;

import com.google.common.collect.Iterators;
import feedme.domain.schedule.timeset.*;
import feedme.util.TimeUtils;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

class TimeSpanSetIntersectOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToAll<TimeSpanSet> {
	TimeSpanSetIntersectOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor, TimeSpanSet thisSet) {
		super(binaryOperationVisitor, thisSet);
	}

	@Override
	public @NotNull TimeSet visit(TimeSpan set) {
		return timeSpanSetIntersectTimeSpan(thisSet, set);
	}

	@Override
	public @NotNull TimeSet visit(TimeSpanSet set) {
		if (thisSet.isEmpty()) {
			return EmptyTimeSet.get();
		}
		if (set.isEmpty()) {
			return EmptyTimeSet.get();
		}
		if (thisSet.size() == 1) {
			return timeSpanSetIntersectTimeSpan(set, thisSet.getFirst().get());
		}
		if (set.size() == 1) {
			return timeSpanSetIntersectTimeSpan(thisSet, set.getFirst().get());
		}

		MutableTimeSet newSet = MutableTimeSet.create();
		Iterator<TimeSpan> merged = Iterators.mergeSorted(List.of(thisSet.iterateForward().iterator(), set.iterateForward().iterator()), TimeSpan::compareStartTimes);
		@NotNull TimeSpan aSpan;
		@NotNull TimeSpan bSpan = merged.next();
		while (merged.hasNext()) {
			aSpan = bSpan;
			bSpan = merged.next();
			if (aSpan.hasOverlapWith(bSpan)) {
				newSet.add(TimeSpan.withBounds(
					TimeUtils.latest(aSpan.start(), bSpan.start()),
					TimeUtils.earliest(aSpan.end(), bSpan.end())
				));
			}
		}

		if (newSet.isEmpty()) {
			return EmptyTimeSet.get();
		}

		return newSet;
	}

	private static @NotNull TimeSet timeSpanSetIntersectTimeSpan(@NotNull TimeSpanSet set, @NotNull TimeSpan span) {
		if (set.isEmpty()) {
			return EmptyTimeSet.get();
		}
		if (set.size() == 1) {
			return set.getFirst().get().intersectWith(span);
		}
		MutableTimeSet newSet = MutableTimeSet.create();
		set.streamForwardFrom(span.start()).takeWhile(setSpan -> setSpan.hasOverlapWith(span)).forEach(setSpan -> {
			if (!span.contains(setSpan)) {
				newSet.add(TimeSpan.withBounds(
					TimeUtils.latest(span.start(), setSpan.start()),
					TimeUtils.earliest(span.end(), setSpan.end())
				));
			} else {
				newSet.add(setSpan);
			}
		});

		// This logic and the one below should be standardized somewhere
		if (newSet.isEmpty()) {
			return EmptyTimeSet.get();
		}
		if (newSet.size() == 1) {
			return newSet.getFirst().orElseThrow();
		}

		return newSet;
	}
}
