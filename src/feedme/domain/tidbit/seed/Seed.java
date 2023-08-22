package feedme.domain.tidbit.seed;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

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
