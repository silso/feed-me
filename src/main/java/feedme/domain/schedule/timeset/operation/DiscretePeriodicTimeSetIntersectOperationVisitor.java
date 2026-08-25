package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import org.jetbrains.annotations.NotNull;

public class DiscretePeriodicTimeSetIntersectOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToAll<DiscretePeriodicTimeSet<?>> {
	DiscretePeriodicTimeSetIntersectOperationVisitor(
		TimeSetBinaryOperationVisitor binaryOperationVisitor,
		DiscretePeriodicTimeSet<?> thisSet
	) {
		super(binaryOperationVisitor, thisSet);
	}

	@Override
	public @NotNull TimeSet visit(TimeSpan set) {
		throw new UnsupportedOperationException("Can't do this union");
	}

	@Override
	public @NotNull TimeSet visit(TimeSpanSet set) {
		throw new UnsupportedOperationException("Can't do this union");
	}
}
