package feedme.domain.tidbit;

import feedme.domain.tidbit.plan.TidbitPlan;
import feedme.domain.tidbit.whiff.Whiff;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

// Converted from record
public class Tidbit {
    private final Instant createdAt;
    private final TidbitState currentState;
    private final TidbitHistory history;
    private final String message;
    private final List<Whiff> whiffs;
    private final TidbitPlan plan;

    public Tidbit(
        Instant createdAt,
        TidbitState currentState,
        TidbitHistory history,
        String message,
        List<Whiff> whiffs,
        TidbitPlan plan
    ) {
        this.createdAt = createdAt;
        this.currentState = currentState;
        this.history = history;
        this.message = message;
        this.whiffs = whiffs;
        this.plan = plan;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public TidbitState currentState() {
        return currentState;
    }

    public TidbitHistory history() {
        return history;
    }

    public String message() {
        return message;
    }

    public List<Whiff> whiffs() {
        return whiffs;
    }

    public TidbitPlan plan() {
        return plan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tidbit tidbit)) return false;
        return Objects.equals(createdAt, tidbit.createdAt) && currentState == tidbit.currentState && Objects.equals(history, tidbit.history) && Objects.equals(message, tidbit.message) && Objects.equals(whiffs, tidbit.whiffs) && Objects.equals(plan, tidbit.plan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, currentState, history, message, whiffs, plan);
    }

    @Override
    public String toString() {
        return "Tidbit[" +
            "createdAt=" + createdAt + ", " +
            "currentState=" + currentState + ", " +
            "history=" + history + ", " +
            "message=" + message + ']';
    }

    public static class TidbitBuilder {
        private Instant createdAt;
        private TidbitState currentState;
        private TidbitHistory history;
        private String message;
        private List<Whiff> whiffs;
        private TidbitPlan plan;

        public TidbitBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TidbitBuilder currentState(TidbitState currentState) {
            this.currentState = currentState;
            return this;
        }

        public TidbitBuilder history(TidbitHistory history) {
            this.history = new TidbitHistory(history);
            return this;
        }

        public TidbitBuilder

        public Tidbit build() {
            return new Tidbit(createdAt, currentState, history, message, whiffs, plan);
        }

        public static TidbitBuilder from(Tidbit tidbit) {
            var instance = new TidbitBuilder();
            instance.createdAt = tidbit.createdAt();
            instance.currentState = tidbit.currentState();
            instance.history = tidbit.history();
            instance.message = tidbit.message();
            instance.whiffs = tidbit.whiffs();
            instance.plan = tidbit.plan();
            return instance;
        }
    }
}
