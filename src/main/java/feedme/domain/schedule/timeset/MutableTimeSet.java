package feedme.domain.schedule.timeset;

import java.util.Collection;

public interface MutableTimeSet extends MeasurableTimeSet {
    boolean add(TimeSpan span);

    boolean addAll(Collection<TimeSpan> spans);

    boolean remove(TimeSpan span);

    static MutableTimeSet create() {
        return new FiniteTimeSet();
    }
}
