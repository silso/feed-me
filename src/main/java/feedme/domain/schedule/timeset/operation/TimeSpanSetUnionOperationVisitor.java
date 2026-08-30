package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import feedme.util.*;
import java.util.*;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class TimeSpanSetUnionOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToAll<TimeSpanSet> {
	TimeSpanSetUnionOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor, TimeSpanSet thisSet) {
		super(binaryOperationVisitor, thisSet);
	}

	@Override
	public @NotNull TimeSet visit(TimeSpan set) {
		return timeSpanSetUnionTimeSpan(thisSet, set);
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
			return timeSpanSetUnionTimeSpan(set, thisSet.getFirst().get());
		}
		if (set.size() == 1) {
			return timeSpanSetUnionTimeSpan(thisSet, set.getFirst().get());
		}

		MutableTimeSet newSet = MutableTimeSet.create();
		// use guava Iterators.mergeSorted
		Stepper<TimeSpan> a = new Stepper<>(thisSet.iterateForward().iterator());
		Stepper<TimeSpan> b = new Stepper<>(set.iterateForward().iterator());
		@Nullable InfInstant start = null;
		while (a.step() | b.step()) {
			if (a.get().isContiguousWith(b.get())) {
				if (start == null) {
					start = TimeUtils.earliest(a.get().start(), b.get().start());
				}
				if (a.get().start().isBefore(b.get().start())) {
					a.shouldStep();
				} else {
					b.shouldStep();
				}
			} else {
				if (a.get().start().isBefore(b.get().start())) {
					if (start != null) {
						newSet.add(TimeSpan.withBounds(start, a.get().end()));
					} else {
						newSet.add(a.get());
					}
					a.shouldStep();
				} else {
					if (start != null) {
						newSet.add(TimeSpan.withBounds(start, b.get().end()));
					} else {
						newSet.add(b.get());
					}
					b.shouldStep();
				}
				start = null;
			}
		}

		if (start != null) {
			newSet.add(
				TimeSpan.withBounds(start, TimeUtils.latest(a.get().end(), b.get().end()))
			);
		}

		return newSet;
	}

	private static TimeSet timeSpanSetUnionTimeSpan(TimeSpanSet first, TimeSpan second) {
		if (first.isEmpty()) {
			return second;
		}
		if (first.size() == 1) {
			return first.getFirst().get().unionWith(second);
		}
		MutableTimeSet set = MutableTimeSet.create();
		Set<TimeSpan> contiguousTimeSpans = new HashSet<>(Collections.singletonList(second));
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

		if (set.size() == 1) {
			return set.getFirst().orElseThrow();
		}

		return set;
	}
}
