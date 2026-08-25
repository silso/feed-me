package feedme.domain.schedule.timeset;

import java.util.Collection;

public interface MutableTimeSet extends CountableTimeSet {
    void add(TimeSpan span);

    void addAll(Collection<TimeSpan> spans);

    void remove(TimeSpan span);

    int size();

    static MutableTimeSet create() {
        return new TimeSpanSet();
    }
}
