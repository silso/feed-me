package feedme.domain.tidbit;

import org.jetbrains.annotations.NotNull;

/**
 * @see Tidbit
 * This is a fake enum class that is used for {@link Tidbit} and enables inheritance.
 */
public class TidbitState {
    public static final TidbitState New = new TidbitState("New");
    public static final TidbitState Visible = new TidbitState("Visible");
    public static final TidbitState Consumed = new TidbitState("Consumed");
    public static final TidbitState Expired = new TidbitState("Expired");

    @NotNull
    public final String name;
    protected TidbitState(@NotNull String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TidbitState that)) return false;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "TidbitState{" +
            "name='" + name + '\'' +
            '}';
    }

    public boolean isFinished() {
        return Consumed.equals(this) || Expired.equals(this);
    }
}
