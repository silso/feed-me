package feedme.domain.schedule;

import feedme.domain.schedule.timeset.TimeSet;

import java.time.Instant;
import java.util.Set;

public interface Schedule<StateType> {
    Set<StateType> getStatesAt(Instant time);

    TimeSet getTimeSetFor(StateType state);
}
