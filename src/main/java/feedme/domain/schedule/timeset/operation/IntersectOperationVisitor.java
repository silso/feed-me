package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import org.jetbrains.annotations.NotNull;

public class IntersectOperationVisitor implements TimeSetBinaryOperationVisitor {
	@Override
	public @NotNull TimeSet visit(TimeSet first, TimeSpan second) {
		return first.accept(new TimeSpanIntersectOperationVisitor(this, second));
	}

	@Override
	public @NotNull TimeSet visit(TimeSet first, TimeSpanSet second) {
		return first.accept(new TimeSpanSetIntersectOperationVisitor(this, second));
	}

	@Override
	public @NotNull TimeSet visit(TimeSet first, CompositeUnionTimeSet second) {
		return first.accept(new CompositeTimeSetOperationVisitors.UnionSet.Intersect(this, second));
	}

	@Override
	public @NotNull TimeSet visit(TimeSet first, CompositeIntersectTimeSet second) {
		return first.accept(new CompositeTimeSetOperationVisitors.IntersectSet.Intersect(this, second));
	}

	@Override
	public @NotNull TimeSet visit(TimeSet first, DiscretePeriodicTimeSet<?> second) {
		return first.accept(new DiscretePeriodicTimeSetIntersectOperationVisitor(this, second));
	}

	@Override
	public @NotNull TimeSet visit(TimeSet first, EmptyTimeSet second) {
		return first.accept(new EmptyTimeSetIntersectOperationVisitor(this));
	}

	@Override
	public @NotNull TimeSet visit(TimeSet first, EverythingTimeSet second) {
		return first.accept(new EverythingTimeSetIntersectOperationVisitor(this));
	}
}
