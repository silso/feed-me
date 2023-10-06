package feedme.domain.tidbit;

import feedme.domain.tidbit.action.TidbitAction;
import feedme.domain.tidbit.action.impl.OnItAction;
import feedme.domain.tidbit.seed.Seed;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TaskTidbit extends Tidbit {

    public TaskTidbit(
        Instant createdAt,
        TidbitState currentState,
        TidbitHistory history,
        String message,
        Seed seed
    ) {
        super(createdAt, currentState, history, message, Type.Task, seed);
    }

    @Override
    public Set<Class<? extends TidbitAction>> allowedActions() {
        Set<Class<? extends TidbitAction>> actions = new HashSet<>(super.allowedActions());
        actions.add(OnItAction.class);
        return Collections.unmodifiableSet(actions);
    }

    @Override
    public Set<Class<? extends TidbitAction>> allowedUserActions() {
        Set<Class<? extends TidbitAction>> actions = new HashSet<>(super.allowedUserActions());
        actions.add(OnItAction.class);
        return Collections.unmodifiableSet(actions);
    }

    public static class State extends TidbitState {
        public static final State OnIt = new State("OnIt");

        protected State(String name) {
            super(name);
        }
    }
}
