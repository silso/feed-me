package feedme.domain.tidbit.seed;

import org.jetbrains.annotations.NotNull;

// fake enum
public class SeedState {
    public static final SeedState New = new SeedState("New");

    @NotNull
    public final String name;
    protected SeedState(@NotNull String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeedState that)) return false;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
