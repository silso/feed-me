package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import feedme.util.InfInstant;
import feedme.util.TimeUtils;
import java.util.*;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class TimeSpanSetUnionOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToAll<TimeSpanSet> {
	TimeSpanSetUnionOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor, TimeSpanSet thisSet) {
		super(binaryOperationVisitor, thisSet);
	}

	@Override
	public @NotNull TimeSet visit(TimeSpan set) {
		return timeSpanSetUnionTimeSegment(thisSet, set);
	}

	@Override
	public @NotNull TimeSet visit(TimeSpanSet set) {
		if (thisSet.isEmpty()) {
			return set;
		}
		if (set.isEmpty()) {
			return thisSet;
		}
		if (thisSet.size() == 1) {
			return timeSpanSetUnionTimeSegment(set, thisSet.getFirst().get());
		}
		if (set.size() == 1) {
			return timeSpanSetUnionTimeSegment(thisSet, set.getFirst().get());
		}

		MutableTimeSet newSet = MutableTimeSet.create();
		Iterator<TimeSpan> a = thisSet.iterateForward().iterator();
		Iterator<TimeSpan> b = thisSet.iterateForward().iterator();
		TimeSpan aSpan = a.next();
		TimeSpan bSpan = b.next();
		@Nullable InfInstant start = null;
		while (a.hasNext() && b.hasNext()) {
			if (aSpan.isContiguousWith(bSpan)) {
				if (start == null) {
					start = TimeUtils.earliest(aSpan.start(), bSpan.start());
				}
				if (aSpan.start().isBefore(bSpan.start())) {
					aSpan = a.next();
				} else {
					bSpan = b.next();
				}
			} else {
				if (aSpan.start().isBefore(bSpan.start())) {
					if (start != null) {
						newSet.add(TimeSpan.withBounds(start, aSpan.end()));
					} else {
						newSet.add(aSpan);
					}
					aSpan = a.next();
				} else {
					if (start != null) {
						newSet.add(TimeSpan.withBounds(start, bSpan.end()));
					} else {
						newSet.add(bSpan);
					}
					bSpan = b.next();
				}
				start = null;
			}
		}
		if (start != null) {
			newSet.add(
				TimeSpan.withBounds(start, TimeUtils.latest(aSpan.end(), bSpan.end()))
			);
		}

		return newSet;
	}

	public static TimeSet timeSpanSetUnionTimeSegment(TimeSpanSet first, TimeSpan second) {
		if (first.isEmpty()) {
			return second;
		}
		if (first.size() == 1) {
			return first.getFirst().get().unionWith(second);
		}
		MutableTimeSet set = MutableTimeSet.create();
		Set<TimeSpan> contiguousTimeSpans = new HashSet<>();
		first.streamForward().forEach(span -> {
			if (span.isContiguousWith(second)) {
				contiguousTimeSpans.add(span);
			} else {
				set.add(span);
			}
		});
		set.add(TimeSpan.withBounds(
			TimeUtils.earliest(contiguousTimeSpans.stream().map(TimeSpan::start).toArray(InfInstant[]::new)),
			TimeUtils.latest(contiguousTimeSpans.stream().map(TimeSpan::end).toArray(InfInstant[]::new))
		));
		return set;
	}
}
