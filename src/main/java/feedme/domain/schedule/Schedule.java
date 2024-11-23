package feedme.domain.schedule;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import feedme.domain.schedule.timeset.TimeSet;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public interface Schedule<StateType> {
    Set<StateType> getStatesAt(Instant time);

    TimeSet getTimeSetFor(StateType state);

    default <NewStateType> Schedule<NewStateType> mapState(StateConverter<StateType, NewStateType> mapper) {
        Schedule<StateType> delegate = Schedule.this;
        return new Schedule<>() {
            @Override
            public Set<NewStateType> getStatesAt(Instant time) {
                return delegate.getStatesAt(time).stream().map(mapper::convertState).collect(Collectors.toSet());
            }

            @Override
            public TimeSet getTimeSetFor(NewStateType state) {
                return delegate.getTimeSetFor(mapper.revertState(state));
            }
        };
    }

    abstract class StateConverter<StateType1, StateType2> {
        public abstract StateType2 convertState(StateType1 from);

        public abstract StateType1 revertState(StateType2 from);
    }

    class OneToOneStateConverter<StateType1, StateType2> extends StateConverter<StateType1, StateType2> {
        private final BiMap<StateType1, StateType2> map = HashBiMap.create();

        public OneToOneStateConverter<StateType1, StateType2> addPair(StateType1 state1, StateType2 state2) {
            map.put(state1, state2);
            return this;
        }

        @Override
        public StateType2 convertState(StateType1 from) {
            return map.get(from);
        }

        @Override
        public StateType1 revertState(StateType2 from) {
            return map.inverse().get(from);
        }
    }
}
