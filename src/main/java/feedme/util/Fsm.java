package feedme.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Stateless finite state machine helper class. Stateless in that state is stored outside of instance
 * and passed in through {@link #step(Object, Object)}
 * @param <State> type to use for state values (whenAt)
 * @param <Input> type that the condition (when) and action callbacks (then, always, otherwise) accept
 */
public class Fsm<State, Input> {
    private final Map<State, List<Transition<Input>>> transitions;

    private Fsm(Map<State, List<Transition<Input>>> transitions) {
        this.transitions = transitions;
    }

    /**
     * Core method to call to progress call the first applicable action (if its condition is true)
     * @param currentState the current state at the step
     * @param input the input to pass to the condition and action
     * @return whether an action was called
     */
    public boolean step(State currentState, Input input) {
        for (Transition<Input> transition : transitions.get(currentState)) {
            if (transition.apply(input)) {
                return true;
            }
        }
        return false;
    }

    public static <State, Input> FsmBuilderRoot<State, Input> create() {
        return new FsmBuilderRoot<>(new HashMap<>());
    }

    private record Transition<Input>(Predicate<Input> predicate, Consumer<Input> action) {
        private boolean apply(Input input) {
            if (predicate.test(input)) {
                action.accept(input);
                return true;
            }
            return false;
        }
    }

    public static class FsmBuilderRoot<State, Input> {
        private final Map<State, List<Transition<Input>>> transitions;
        private FsmBuilderRoot(Map<State, List<Transition<Input>>> transitions) {
            this.transitions = transitions;
        }

        public FsmBuilderStateFirst<State, Input> whenAt(State state) {
            transitions.putIfAbsent(state, new ArrayList<>());
            return new FsmBuilderStateFirst<>(transitions, state);
        }

        public Fsm<State, Input> build() {
            return new Fsm<>(transitions);
        }
    }

    public static class FsmBuilderStateFirst<State, Input> {
        private final Map<State, List<Transition<Input>>> transitions;
        private final State atState;
        private FsmBuilderStateFirst(Map<State, List<Transition<Input>>> transitions, State atState) {
            this.transitions = transitions;
            this.atState = atState;
        }
        public FsmBuilderTransition<State, Input> when(Predicate<Input> predicate) {
            return new FsmBuilderTransition<>(transitions, atState, predicate);
        }

        public FsmBuilderRoot<State, Input> always(Runnable action) {
            return always(input -> action.run());
        }

        public FsmBuilderRoot<State, Input> always(Consumer<Input> action) {
            Transition<Input> transition = new Transition<>((state) -> true, action);
            transitions.get(atState).add(transition);
            return new FsmBuilderRoot<>(transitions);
        }
    }

    public static class FsmBuilderStateAfter<State, Input> extends FsmBuilderRoot<State, Input> {
        private final Map<State, List<Transition<Input>>> transitions;
        private final State atState;
        private FsmBuilderStateAfter(Map<State, List<Transition<Input>>> transitions, State atState) {
            super(transitions);
            this.transitions = transitions;
            this.atState = atState;
        }
        public FsmBuilderTransition<State, Input> when(Predicate<Input> predicate) {
            return new FsmBuilderTransition<>(transitions, atState, predicate);
        }

        public FsmBuilderRoot<State, Input> otherwise(Runnable action) {
            return otherwise(input -> action.run());
        }

        public FsmBuilderRoot<State, Input> otherwise(Consumer<Input> action) {
            Transition<Input> transition = new Transition<>((state) -> true, action);
            transitions.get(atState).add(transition);
            return new FsmBuilderRoot<>(transitions);
        }
    }

    public static class FsmBuilderTransition<State, Input> {
        private final Map<State, List<Transition<Input>>> transitions;
        private final State atState;
        private final Predicate<Input> predicate;
        private FsmBuilderTransition(Map<State, List<Transition<Input>>> transitions, State atState, Predicate<Input> predicate) {
            this.transitions = transitions;
            this.atState = atState;
            this.predicate = predicate;
        }
        public FsmBuilderStateAfter<State, Input> then(Runnable action) {
            return then(input -> action.run());
        }

        public FsmBuilderStateAfter<State, Input> then(Consumer<Input> action) {
            Transition<Input> transition = new Transition<>(predicate, action);
            transitions.get(atState).add(transition);
            return new FsmBuilderStateAfter<>(transitions, atState);
        }
    }
}
