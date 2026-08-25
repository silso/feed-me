package feedme.domain.schedule.timeset.operation;

import feedme.domain.schedule.timeset.*;
import feedme.util.TimeUtils;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TimeSpanUnionOperationVisitor extends CurriedTsBinaryOpVisitor.DelegateToTimeSpanSet<TimeSpan> {
    TimeSpanUnionOperationVisitor(TimeSetBinaryOperationVisitor binaryOperationVisitor, TimeSpan thisSet) {
        super(binaryOperationVisitor, thisSet);
    }

    @Override
    public @NotNull TimeSet visit(TimeSpan set) {
        if (thisSet.isContiguousWith(set)) {
            if (
                TimeUtils.earliest(thisSet.start(), set.start()).isInfinitePast() &&
                TimeUtils.latest(thisSet.end(), set.end()).isInfiniteFuture()
            ) {
                return EverythingTimeSet.get();
            }
            return TimeSpan.withBounds(
                TimeUtils.earliest(thisSet.start(), set.start()),
                TimeUtils.latest(thisSet.end(), set.end())
            );
        } else {
            MutableTimeSet newSet = MutableTimeSet.create();
            newSet.addAll(List.of(thisSet, set));
            return newSet;
        }
    }
}
