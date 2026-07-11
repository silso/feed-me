package feedme.domain.schedule.timeset;

import java.util.Collection;

public interface MutableTimeSet extends MeasurableTimeSet {
    void add(TimeSpan span);

    void addAll(Collection<TimeSpan> spans);

    void remove(TimeSpan span);

    static MutableTimeSet create() {
        return new FiniteTimeSet();
    }
}
