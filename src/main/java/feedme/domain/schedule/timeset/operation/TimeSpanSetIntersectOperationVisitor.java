package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import feedme.util.InfInstant;
import feedme.util.TimeUtils;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

class TimeSpanSetIntersectOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToAll<TimeSpanSet> {
	TimeSpanSetIntersectOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor, TimeSpanSet thisSet) {
		super(binaryOperationVisitor, thisSet);
	}

	@Override
	public @NotNull TimeSet visit(TimeSpan set) {
		// TODO: implement
		if (thisSet.isEmpty()) {
			return EmptyTimeSet.get();
		}
		if (thisSet.size() == 1) {
			return thisSet.getFirst().get().intersectWith(set);
		}
		MutableTimeSet set1 = MutableTimeSet.create();
		Set<TimeSpan> contiguousTimeSpans = new HashSet<>();
		thisSet.streamForward().forEach(span -> {
			if (span.isContiguousWith(set)) {
				contiguousTimeSpans.add(span);
			} else {
				set1.add(span);
			}
		});
		set1.add(TimeSpan.withBounds(
			TimeUtils.earliest(contiguousTimeSpans.stream().map(TimeSpan::start).toArray(InfInstant[]::new)),
			TimeUtils.latest(contiguousTimeSpans.stream().map(TimeSpan::end).toArray(InfInstant[]::new))
		));
		return set1;
	}

	@Override
	public @NotNull TimeSet visit(TimeSpanSet set) {
		return null;
	}

}
