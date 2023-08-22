package feedme.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Fsm<State, Input> {
    private final Map<State, List<Transition<Input>>> transitions;

    private Fsm(Map<State, List<Transition<Input>>> transitions) {
        this.transitions = transitions;
    }

    public boolean step(State currentState, Input input) {
        for (Transition<Input> transition : transitions.get(currentState)) {
            if (transition.apply(input)) {
                return true;
            }
        }
        return false;
    }

    private record Transition<Input>(Predicate<Input> predicate, Consumer<Input> action) {
        public boolean apply(Input input) {
            if (predicate.test(input)) {
                action.accept(input);
                return true;
            }
            return false;
        }
    }

    public static <State, Input> FsmBuilderRoot<State, Input> create() {
        return new FsmBuilderRoot<>(new HashMap<>());
    }

    public record FsmBuilderRoot<State, Input>(Map<State, List<Transition<Input>>> transitions) {
        public FsmBuilderStateFirst<State, Input> whenAt(State state) {
            transitions.putIfAbsent(state, new ArrayList<>());
            return new FsmBuilderStateFirst<>(transitions, state);
        }

        public Fsm<State, Input> build() {
            return new Fsm<>(transitions);
        }
    }

    public record FsmBuilderStateFirst<State, Input>(Map<State, List<Transition<Input>>> transitions, State atState) {
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

    public record FsmBuilderStateAfter<State, Input>(Map<State, List<Transition<Input>>> transitions, State atState) {
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

        public FsmBuilderStateFirst<State, Input> whenAt(State state) {
            transitions.putIfAbsent(state, new ArrayList<>());
            return new FsmBuilderStateFirst<>(transitions, state);
        }

        public Fsm<State, Input> build() {
            return new Fsm<>(transitions);
        }
    }

    public record FsmBuilderTransition<State, Input>(Map<State, List<Transition<Input>>> transitions, State atState, Predicate<Input> predicate) {
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
