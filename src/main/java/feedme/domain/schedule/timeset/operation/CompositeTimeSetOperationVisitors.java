package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

final class CompositeTimeSetOperationVisitors {
	private CompositeTimeSetOperationVisitors() {}

	static CompositeTimeSet mergeUnion(CompositeUnionTimeSet first, CompositeUnionTimeSet second) {
		return new CompositeUnionTimeSet(Stream.concat(first.getSets().stream(), second.getSets().stream()).toList());
	}

	static CompositeTimeSet appendUnion(CompositeUnionTimeSet first, TimeSet second) {
		return new CompositeUnionTimeSet(Stream.concat(first.getSets().stream(), Stream.of(second)).toList());
	}

	static CompositeTimeSet naiveUnion(TimeSet first, TimeSet second) {
		return new CompositeUnionTimeSet(List.of(first, second));
	}

	static CompositeTimeSet mergeIntersect(CompositeIntersectTimeSet first, CompositeIntersectTimeSet second) {
		return new CompositeIntersectTimeSet(Stream.concat(first.getSets().stream(), second.getSets().stream()).toList());
	}

	static CompositeTimeSet appendIntersect(CompositeIntersectTimeSet first, TimeSet second) {
		return new CompositeIntersectTimeSet(Stream.concat(first.getSets().stream(), Stream.of(second)).toList());
	}

	static CompositeTimeSet naiveIntersect(TimeSet first, TimeSet second) {
		return new CompositeIntersectTimeSet(List.of(first, second));
	}

	static final class UnionSet {
		private UnionSet() {}

		static class Union extends CurriedTsBinaryOpVisitor.DelegateToEverything<CompositeUnionTimeSet> {
			Union(TimeSetBinaryOperationVisitor binaryOperationVisitor, CompositeUnionTimeSet thisSet) {
				super(binaryOperationVisitor, thisSet);
			}

			@Override
			public @NotNull TimeSet visit(TimeSpan set) {
				return appendUnion(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(TimeSpanSet set) {
				return appendUnion(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(CompositeUnionTimeSet set) {
				return mergeUnion(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(CompositeIntersectTimeSet set) {
				return appendUnion(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(TimeOfDayTimeSet set) {
				return appendUnion(thisSet, set);
			}
		}

		static class Intersect extends CurriedTsBinaryOpVisitor.DelegateToEverything<CompositeUnionTimeSet> {
			Intersect(TimeSetBinaryOperationVisitor binaryOperationVisitor, CompositeUnionTimeSet thisSet) {
				super(binaryOperationVisitor, thisSet);
			}

			@Override
			public @NotNull TimeSet visit(TimeSpan set) {
				return naiveIntersect(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(TimeSpanSet set) {
				return naiveIntersect(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(CompositeUnionTimeSet set) {
				return naiveIntersect(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(CompositeIntersectTimeSet set) {
				return appendIntersect(set, thisSet);
			}

			@Override
			public @NotNull TimeSet visit(TimeOfDayTimeSet set) {
				return naiveIntersect(thisSet, set);
			}
		}
	}

	static final class IntersectSet {
		private IntersectSet() {}

		static class Union extends CurriedTsBinaryOpVisitor.DelegateToCompositeUnion<CompositeIntersectTimeSet> {
			Union(TimeSetBinaryOperationVisitor binaryOperationVisitor, CompositeIntersectTimeSet thisSet) {
				super(binaryOperationVisitor, thisSet);
			}

			@Override
			public @NotNull TimeSet visit(TimeSpan set) {
				return naiveUnion(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(TimeSpanSet set) {
				return naiveUnion(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(CompositeIntersectTimeSet set) {
				return naiveUnion(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(TimeOfDayTimeSet set) {
				return naiveUnion(thisSet, set);
			}
		}

		static class Intersect extends CurriedTsBinaryOpVisitor.DelegateToCompositeUnion<CompositeIntersectTimeSet> {
			Intersect(TimeSetBinaryOperationVisitor binaryOperationVisitor, CompositeIntersectTimeSet thisSet) {
				super(binaryOperationVisitor, thisSet);
			}

			@Override
			public @NotNull TimeSet visit(TimeSpan set) {
				return appendIntersect(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(TimeSpanSet set) {
				return appendIntersect(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(CompositeIntersectTimeSet set) {
				return mergeIntersect(thisSet, set);
			}

			@Override
			public @NotNull TimeSet visit(TimeOfDayTimeSet set) {
				return appendIntersect(thisSet, set);
			}
		}
	}
}
