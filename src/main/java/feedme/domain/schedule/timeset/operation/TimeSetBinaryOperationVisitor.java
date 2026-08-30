package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import org.jetbrains.annotations.NotNull;

// TODO: enable equality operator? need lots of generics for that, but right now equality is a little wrong
public interface TimeSetBinaryOperationVisitor {
    @NotNull TimeSet visit(TimeSet first, TimeSpan second);
    @NotNull TimeSet visit(TimeSet first, TimeSpanSet second);
    @NotNull TimeSet visit(TimeSet first, CompositeUnionTimeSet second);
    @NotNull TimeSet visit(TimeSet first, CompositeIntersectTimeSet second);
    @NotNull TimeSet visit(TimeSet first, DiscretePeriodicTimeSet<?> second);
    default @NotNull TimeSet visit(TimeSet first, TimeOfDayTimeSet second) {
        throw new UnsupportedOperationException("Unimplemented");
    }
    @NotNull TimeSet visit(TimeSet first, DurationTimeSet second);

    @NotNull TimeSet visit(TimeSet first, EmptyTimeSet second);
    @NotNull TimeSet visit(TimeSet first, EverythingTimeSet second);
}
