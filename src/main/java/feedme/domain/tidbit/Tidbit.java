package feedme.domain.tidbit;

import com.google.common.base.Objects;
import feedme.domain.tidbit.action.TidbitAction;
import feedme.domain.tidbit.action.impl.ConsumeAction;
import feedme.domain.tidbit.action.impl.EmitAction;
import feedme.domain.tidbit.action.impl.ExpireAction;
import feedme.domain.tidbit.seed.Seed;
import feedme.domain.tidbit.urgency.Urgency;

import java.time.Instant;
import java.util.Set;

/**
 * A piece of information that should be shown to the user.
 * <p>
 * The user and the app can perform actions {@link TidbitAction} on the Tidbit which can change the tidbit's
 * {@link #currentState} and add that action to the {@link #history}. The associated {@link #seed} is what created
 * this tidbit and likely what controls its state.
 * The information should be shown to the user typically when it becomes visible ({@link TidbitState#Visible}.
 * When the tidbit is past its useful date, it is {@link TidbitState#Consumed} if due to the user actually absorbing
 * or completing the associated information, otherwise {@link TidbitState#Expired}
 */
public abstract class Tidbit {
    public final Instant createdAt;
    public TidbitState currentState;
    public final TidbitHistory history;
    public String message;
    public final Seed seed;
    public final Urgency urgency;

    public Tidbit(Instant createdAt, TidbitState currentState, TidbitHistory history, String message, Seed seed, Urgency urgency) {
        this.createdAt = createdAt;
        this.currentState = currentState;
        this.history = history;
        this.message = message;
        this.seed = seed;
        this.urgency = urgency;
    }

    public Set<Class<? extends TidbitAction>> allowedActions() {
        return Set.of(
            EmitAction.class,
            ConsumeAction.class,
            ExpireAction.class
        );
    }

    public Set<Class<? extends TidbitAction>> allowedUserActions() {
        return Set.of(
            ConsumeAction.class
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tidbit tidbit)) return false;
        return Objects.equal(createdAt, tidbit.createdAt) && Objects.equal(currentState, tidbit.currentState) && Objects.equal(history, tidbit.history) && Objects.equal(message, tidbit.message) && Objects.equal(seed, tidbit.seed) && Objects.equal(urgency, tidbit.urgency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(createdAt, currentState, history, message, seed, urgency);
    }

    @Override
    public String toString() {
        return "Tidbit{" +
            "createdAt=" + createdAt +
            ", currentState=" + currentState +
            ", message='" + message + '\'' +
            '}';
    }
}
