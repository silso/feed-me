package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import org.jetbrains.annotations.NotNull;

public class EmptyTimeSetUnionOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToNone<EmptyTimeSet> {
	public EmptyTimeSetUnionOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor) {
		super(binaryOperationVisitor, EmptyTimeSet.get());
	}

	@Override
	public @NotNull TimeSet visit(TimeSpan set) {
		return set;
	}

	@Override
	public @NotNull TimeSet visit(TimeSpanSet set) {
		return set;
	}

	@Override
	public @NotNull TimeSet visit(CompositeUnionTimeSet set) {
		return set;
	}

	@Override
	public @NotNull TimeSet visit(CompositeIntersectTimeSet set) {
		return set;
	}

	@Override
	public @NotNull TimeSet visit(TimeOfDayTimeSet set) {
		return set;
	}

	@Override
	public @NotNull TimeSet visit(EmptyTimeSet set) {
		return set;
	}

	@Override
	public @NotNull TimeSet visit(EverythingTimeSet set) {
		return set;
	}
}
