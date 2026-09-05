package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import org.jetbrains.annotations.NotNull;

class EmptyTimeSetIntersectOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToNone<EmptyTimeSet> {
	public EmptyTimeSetIntersectOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor) {
		super(binaryOperationVisitor, EmptyTimeSet.get());
	}

	@Override
	public @NotNull TimeSet visit(TimeSpan set) {
		return thisSet;
	}

	@Override
	public @NotNull TimeSet visit(TimeSpanSet set) {
		return thisSet;
	}

	@Override
	public @NotNull TimeSet visit(CompositeUnionTimeSet set) {
		return thisSet;
	}

	@Override
	public @NotNull TimeSet visit(CompositeIntersectTimeSet set) {
		return thisSet;
	}

	@Override
	public @NotNull TimeSet visit(TimeOfDayTimeSet set) {
		return thisSet;
	}

	@Override
	public @NotNull TimeSet visit(EmptyTimeSet set) {
		return thisSet;
	}

	@Override
	public @NotNull TimeSet visit(EverythingTimeSet set) {
		throw new UnsupportedOperationException("Cannot intersect empty set with everything set");
	}
}
