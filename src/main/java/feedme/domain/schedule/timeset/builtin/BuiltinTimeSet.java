package feedme.domain.schedule.timeset.builtin;

import feedme.domain.schedule.timeset.TimeSet;
import feedme.domain.schedule.timeset.WeekdaysTimeSet;

import java.util.TimeZone;

public class BuiltinTimeSet {
    public static final TimeSet WEEKDAYS = new WeekdaysTimeSet(TimeZone.getDefault());
}
