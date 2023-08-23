package feedme.domain.tidbit.seed;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * What creates tidbits. Contains the logic for creating, emitting, expiring, and doing other actions
 * on tidbits.
 */
public abstract class Seed {
    public abstract void createTidbits(@NotNull Instant now);

    public enum Type {
        Base("Base"),
        Task("Task");

        public final String name;
        Type(String name) {
            this.name = name;
        }
    }
}
