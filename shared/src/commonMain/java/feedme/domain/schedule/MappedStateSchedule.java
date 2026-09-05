package feedme.domain.schedule;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import feedme.domain.schedule.timeset.TimeSet;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public class MappedStateSchedule<FromState, ToState> implements Schedule<ToState> {
    private final Schedule<FromState> originalSchedule;
    private final StateConverter<FromState, ToState> stateConverter;

    public MappedStateSchedule(Schedule<FromState> originalSchedule, StateConverter<FromState, ToState> stateConverter) {
        this.originalSchedule = originalSchedule;
        this.stateConverter = stateConverter;
    }

    @Override
    public Set<ToState> getStatesAt(Instant time) {
        return originalSchedule.getStatesAt(time).stream().map(stateConverter::convertState).collect(Collectors.toSet());
    }

    @Override
    public TimeSet getTimeSetFor(ToState state) {
        return originalSchedule.getTimeSetFor(stateConverter.revertState(state));
    }

    public static <FromState, ToState> MappedStateScheduleBuilder<FromState, ToState> builder(Schedule<FromState> originalSchedule, Class<ToState> toStateClass) {
        return new MappedStateScheduleBuilder<>(originalSchedule);
    }

    public static class MappedStateScheduleBuilder<FromState, ToState> {
        private final OneToOneStateConverter<FromState, ToState> converter = new OneToOneStateConverter<>();
        private final Schedule<FromState> originalSchedule;

        private MappedStateScheduleBuilder(Schedule<FromState> originalSchedule) {
            this.originalSchedule = originalSchedule;
        }

        public MappedStateScheduleBuilder<FromState, ToState> addPair(FromState fromState, ToState toState) {
            converter.addPair(fromState, toState);
            return this;
        }

        public MappedStateSchedule<FromState, ToState> build() {
            return new MappedStateSchedule<>(originalSchedule, converter);
        }
    }

    public interface StateConverter<FromState, ToState> {
        ToState convertState(FromState from);
        FromState revertState(ToState from);
    }

    public static class OneToOneStateConverter<FromState, ToState> implements StateConverter<FromState, ToState> {
        private final BiMap<FromState, ToState> map = HashBiMap.create();

        public OneToOneStateConverter<FromState, ToState> addPair(FromState fromState, ToState toState) {
            map.put(fromState, toState);
            return this;
        }

        @Override
        public ToState convertState(FromState from) {
            return map.get(from);
        }

        @Override
        public FromState revertState(ToState from) {
            return map.inverse().get(from);
        }
    }
}
