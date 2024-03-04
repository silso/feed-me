package feedme.domain.schedule;

import feedme.domain.schedule.timeset.TimeSet;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class LayeredStateSchedule<StateType> implements Schedule<StateType> {
    private final SequencedCollection<SingleStateSchedule<StateType>> rules;

    protected LayeredStateSchedule(@NotNull SequencedCollection<SingleStateSchedule<StateType>> rules) {
        this.rules = rules;
    }

    public static <StateType> LayeredStateScheduleBuilder<StateType> create() {
        return new LayeredStateScheduleBuilder<>();
    }

    public static <StateType> LayeredStateScheduleBuilder<StateType> createWithBaseState(@NotNull StateType baseState) {
        return new LayeredStateScheduleBuilder<StateType>().add(baseState, TimeSet.EVERYTHING);
    }

    @Override
    public Set<StateType> getStatesAt(Instant time) {
        return rules
            .reversed()
            .stream()
            .map(schedule -> schedule.getAt(time))
            .flatMap(Optional::stream)
            .findFirst()
            .stream()
            .collect(Collectors.toSet());
    }

    @Override
    public TimeSet getTimeSetFor(StateType state) {
        TimeSet set = TimeSet.EMPTY;
        for (SingleStateSchedule<StateType> rule : rules) {
            if (rule.state().equals(state)) {
                set = set.unionWith(rule.timeSet());
            } else {
                set = set.differenceWith(rule.timeSet());
            }
        }
        return set;
    }

    public static class LayeredStateScheduleBuilder<StateType> {
        private final SequencedCollection<SingleStateSchedule<StateType>> rules = new ArrayList<>();

        private LayeredStateScheduleBuilder() {}

        public LayeredStateScheduleBuilder<StateType> add(@NotNull StateType state, @NotNull TimeSet timeSet) {
            this.rules.addLast(new SingleStateSchedule<>(state, timeSet));
            return this;
        }

        public LayeredStateSchedule<StateType> build() {
            return new LayeredStateSchedule<>(rules);
        }
    }
}
