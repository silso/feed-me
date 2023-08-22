package feedme.domain.tidbit;

import feedme.domain.tidbit.action.TidbitAction;
import feedme.domain.tidbit.seed.Seed;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public abstract class Tidbit<Type extends Tidbit.Type> {
    public final Instant createdAt;
    public TidbitState currentState;
    public final TidbitHistory history;
    public String message;
    public final Type type;
    public final Seed seed;

    public Tidbit(
        Instant createdAt,
        TidbitState currentState,
        TidbitHistory history,
        String message,
        Type type,
        Seed seed
    ) {
        this.createdAt = createdAt;
        this.currentState = currentState;
        this.history = history;
        this.message = message;
        this.type = type;
        this.seed = seed;
    }

    public abstract Set<Class<? extends TidbitAction>> availableActions();

    @Override
    public String toString() {
        return "Tidbit{" +
            "createdAt=" + createdAt +
            ", currentState=" + currentState +
            ", history=" + history +
            ", message='" + message + '\'' +
            '}';
    }

    public enum Type {
        Base("Base"),
        Task("Task");

        public final String name;
        Type(String name) {
            this.name = name;
        }
    }
}
