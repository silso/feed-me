package feedme.domain.tidbit.whiff;

import feedme.domain.tidbit.whiff.action.WhiffAction;
import feedme.domain.tidbit.whiff.impl.TaskWhiff;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Whiff {
    String message();
    State currentState();
    Set<WhiffAction.Impl> availableActions();

    enum State {
        Scheduled,
        Emitted,
        Inhaled,
    }

    enum Impl {
        Task(TaskWhiff::new);

        private final BiFunction<String, State, Whiff> constructor;
        Impl(BiFunction<String, State, Whiff> constructor) {
            this.constructor = constructor;
        }

        public Whiff create(String message, State currentState) {
            return constructor.apply(message, currentState);
        }
    }
}
