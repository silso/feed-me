package feedme.domain.tidbit;

import feedme.domain.tidbit.plan.TidbitPlan;
import feedme.domain.tidbit.whiff.Whiff;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class TaskTidbit extends Tidbit {
    private final Instant expiresAt;
    public TaskTidbit(
        Instant createdAt,
        TidbitState currentState,
        TidbitHistory history,
        String message,
        List<Whiff> whiffs,
        TidbitPlan plan,
        Instant expiresAt
    ) {
        super(createdAt, currentState, history, message, whiffs, plan);
        this.expiresAt = expiresAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskTidbit that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expiresAt);
    }

    @Override
    public String toString() {
        return "TaskTidbit{" +
            "expiresAt=" + expiresAt +
            '}';
    }
}
