package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import org.jetbrains.annotations.NotNull;

public class EverythingTimeSetUnionOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToEmpty<EverythingTimeSet> {
	EverythingTimeSetUnionOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor) {
		super(binaryOperationVisitor, EverythingTimeSet.get());
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
	public @NotNull TimeSet visit(EverythingTimeSet set) {
		return thisSet;
	}
}
