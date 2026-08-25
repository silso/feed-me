package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import org.jetbrains.annotations.NotNull;

abstract class CurriedTsBinaryOpVisitor<ThisSet extends TimeSet> implements TimeSetUnaryOperationVisitor {
	protected final TimeSetBinaryOperationVisitor binaryOperationVisitor;
	protected final ThisSet thisSet;

	private CurriedTsBinaryOpVisitor(TimeSetBinaryOperationVisitor binaryOpVisitor, ThisSet thisSet) {
		this.binaryOperationVisitor = binaryOpVisitor;
		this.thisSet = thisSet;
	}

	static abstract class DelegateToNone<T extends TimeSet> extends CurriedTsBinaryOpVisitor<T> {
		DelegateToNone(TimeSetBinaryOperationVisitor binaryOperationVisitor, T thisSet) {
			super(binaryOperationVisitor, thisSet);
		}
	}

	static abstract class DelegateToEmpty<T extends TimeSet> extends DelegateToNone<T> {
		DelegateToEmpty(TimeSetBinaryOperationVisitor binaryOperationVisitor, T thisSet) {
			super(binaryOperationVisitor, thisSet);
		}

		@Override
		public final @NotNull TimeSet visit(EmptyTimeSet set) {
			return set.accept(binaryOperationVisitor, thisSet);
		}
	}

	static abstract class DelegateToEverything<T extends TimeSet> extends DelegateToEmpty<T> {
		DelegateToEverything(TimeSetBinaryOperationVisitor binaryOperationVisitor, T thisSet) {
			super(binaryOperationVisitor, thisSet);
		}

		@Override
		public final @NotNull TimeSet visit(EverythingTimeSet set) {
			return set.accept(binaryOperationVisitor, thisSet);
		}
	}

	static abstract class DelegateToCompositeUnion<T extends TimeSet> extends DelegateToEverything<T> {
		DelegateToCompositeUnion(TimeSetBinaryOperationVisitor binaryOperationVisitor, T thisSet) {
			super(binaryOperationVisitor, thisSet);
		}

		@Override
		public final @NotNull TimeSet visit(CompositeUnionTimeSet set) {
			return set.accept(binaryOperationVisitor, thisSet);
		}
	}

	static abstract class DelegateToCompositeIntersect<T extends TimeSet> extends DelegateToCompositeUnion<T> {
		DelegateToCompositeIntersect(TimeSetBinaryOperationVisitor binaryOperationVisitor, T thisSet) {
			super(binaryOperationVisitor, thisSet);
		}

		@Override
		public final @NotNull TimeSet visit(CompositeIntersectTimeSet set) {
			return set.accept(binaryOperationVisitor, thisSet);
		}
	}

	static abstract class DelegateToAll<T extends TimeSet> extends DelegateToCompositeIntersect<T> {
		DelegateToAll(TimeSetBinaryOperationVisitor binaryOperationVisitor, T thisSet) {
			super(binaryOperationVisitor, thisSet);
		}
	}

	// Separate delegations (unsure if I want this)
	static abstract class DelegateToTimeSpanSet<T extends TimeSet> extends DelegateToAll<T> {
		DelegateToTimeSpanSet(TimeSetBinaryOperationVisitor binaryOperationVisitor, T thisSet) {
			super(binaryOperationVisitor, thisSet);
		}

		@Override
		public final @NotNull TimeSet visit(TimeSpanSet set) {
			return set.accept(binaryOperationVisitor, thisSet);
		}
	}
}
