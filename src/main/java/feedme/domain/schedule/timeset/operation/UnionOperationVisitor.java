package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class UnionOperationVisitor implements TimeSetBinaryOperationVisitor {
    @Override
    public @NotNull TimeSet visit(TimeSet first, TimeSpan second) {
        return first.accept(new TimeSpanUnionOperationVisitor(this, second));
    }

    @Override
    public @NotNull TimeSet visit(TimeSet first, TimeSpanSet second) {
        return first.accept(new TimeSpanSetUnionOperationVisitor(this, second));
    }

    @Override
    public @NotNull TimeSet visit(TimeSet first, CompositeUnionTimeSet second) {
        return first.accept(new CompositeTimeSetOperationVisitors.UnionSet.Union(this, second));
    }

    @Override
    public @NotNull TimeSet visit(TimeSet first, CompositeIntersectTimeSet second) {
        return first.accept(new CompositeTimeSetOperationVisitors.IntersectSet.Union(this, second));
    }

    @Override
    public @NotNull TimeSet visit(TimeSet first, DiscretePeriodicTimeSet<?> second) {
        return first.accept(new DiscretePeriodicTimeSetUnionOperationVisitor(this, second));
    }

    @Override
    public @NotNull TimeSet visit(TimeSet first, TimeOfDayTimeSet second) {
        return new CompositeUnionTimeSet(List.of(first, second));
    }

    @Override
    public @NotNull TimeSet visit(TimeSet first, DurationTimeSet second) {
        return new CompositeUnionTimeSet(List.of(first, second));
    }

    @Override
    public @NotNull TimeSet visit(TimeSet first, EmptyTimeSet second) {
        return first.accept(new EmptyTimeSetUnionOperationVisitor(this));
    }

    @Override
    public @NotNull TimeSet visit(TimeSet first, EverythingTimeSet second) {
        return first.accept(new EverythingTimeSetUnionOperationVisitor(this));
    }
}
