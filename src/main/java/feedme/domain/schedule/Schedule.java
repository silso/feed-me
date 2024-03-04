package feedme.domain.schedule;

import com.google.common.collect.Lists;
import feedme.domain.schedule.timeset.Periodic;
import feedme.domain.schedule.timeset.TimeSpan;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Schedule<StateType> {
    private final StateType baseState;
    private final List<ScheduleRule<StateType>> rules = new ArrayList<>();

    protected Schedule(StateType baseState, List<ScheduleRule<StateType>> rules) {
        this.baseState = baseState;
        this.rules.addAll(rules);
    }

    public StateType findCurrentState(Instant now) {
        return Lists
            .reverse(rules)
            .stream()
            .filter(rule -> rule.appliesTo(now))
            .findFirst()
            .map(ScheduleRule::state)
            .orElse(baseState);
    }

    public Optional<TimeSpan> findNextTimeSpanWithState(StateType state, Instant before) {
        // not totally sure how to implement yet, tricky
        return Optional.empty();
    }

    public static <StateType> Schedule<StateType> create(StateType baseState, List<ScheduleRule<StateType>> rules) {
        return new Schedule<>(baseState, rules);
    }

    public record ScheduleRule<RuleStateType>(RuleStateType state, Periodic onTime, Periodic offTime) {
        public boolean appliesTo(Instant time) {
            return onTime.getPrevious(time).isAfter(offTime.getPrevious(time));
        }

        public Optional<TimeSpan> timeSpanAt(Instant time) {
            Instant prevOnTime = onTime.getPrevious(time);
            if (prevOnTime.isAfter(offTime.getPrevious(time))) {
                return Optional.of(TimeSpan.ofInstants(prevOnTime, offTime.getNext(time)));
            } else {
                return Optional.empty();
            }
        }
    }
}
