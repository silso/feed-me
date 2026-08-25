package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import feedme.util.InfInstant;
import feedme.util.TimeUtils;
import org.jetbrains.annotations.NotNull;

class TimeSpanIntersectOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToTimeSpanSet<TimeSpan> {
	TimeSpanIntersectOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor, TimeSpan thisSet) {
		super(binaryOperationVisitor, thisSet);
	}

	@Override
	public @NotNull TimeSet visit(TimeSpan set) {
		InfInstant start = TimeUtils.latest(thisSet.start(), set.start());
		InfInstant end = TimeUtils.earliest(thisSet.end(), set.end());
		if (start.isBefore(end)) {
			return TimeSpan.withBounds(start, end);
		} else {
			return EmptyTimeSet.get();
		}
	}
}
