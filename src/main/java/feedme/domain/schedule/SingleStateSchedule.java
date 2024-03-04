package feedme.domain.schedule;

import feedme.domain.schedule.timeset.TimeSet;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record SingleStateSchedule<StateType>(@NotNull StateType state, @NotNull TimeSet timeSet) implements Schedule<StateType> {

    public @NotNull Optional<StateType> getAt(Instant time) {
        return timeSet.getAt(time).map(span -> state);
    }

    @Override
    public Set<StateType> getStatesAt(Instant time) {
        return getAt(time)
            .stream()
            .collect(Collectors.toSet());
    }

    @Override
    public TimeSet getTimeSetFor(StateType state) {
        if (this.state.equals(state)) {
            return timeSet;
        } else {
            return TimeSet.EMPTY;
        }
    }
}
