package feedme.domain.schedule.timeset.builtin;

import feedme.domain.schedule.timeset.*;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.TimeZone;

public class BuiltinTimeSet {
    public static final TimeSet WEEKDAYS = new WeekdaysTimeSet(TimeZone.getDefault());
    public static final TimeSet WEEKENDS = new DayOfWeekTimeSet(TimeZone.getDefault(), Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
}
