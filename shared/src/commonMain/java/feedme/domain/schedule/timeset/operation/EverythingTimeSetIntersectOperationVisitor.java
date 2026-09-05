package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import org.jetbrains.annotations.NotNull;

public class EverythingTimeSetIntersectOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToEmpty<EverythingTimeSet> {

	EverythingTimeSetIntersectOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor) {
		super(binaryOperationVisitor, EverythingTimeSet.get());
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
	public @NotNull TimeSet visit(EverythingTimeSet set) {
		return set;
	}
}
