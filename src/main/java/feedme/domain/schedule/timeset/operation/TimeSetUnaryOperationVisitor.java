package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import org.jetbrains.annotations.NotNull;

public interface TimeSetUnaryOperationVisitor {
    @NotNull TimeSet visit(TimeSpan set);
    @NotNull TimeSet visit(TimeSpanSet set);
    @NotNull TimeSet visit(CompositeUnionTimeSet set);
    @NotNull TimeSet visit(CompositeIntersectTimeSet set);
    default @NotNull TimeSet visit(DiscretePeriodicTimeSet<?> set) {
        throw new UnsupportedOperationException("Unimplemented");
    }
    @NotNull TimeSet visit(TimeOfDayTimeSet set);
    default @NotNull TimeSet visit(DurationTimeSet set) {
        // unreachable because this just uses composite
        throw new UnsupportedOperationException("Unimplemented");
    }

    @NotNull TimeSet visit(EmptyTimeSet set);
    @NotNull TimeSet visit(EverythingTimeSet set);
}
