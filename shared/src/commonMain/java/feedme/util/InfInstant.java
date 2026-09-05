package feedme.util;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class InfInstant implements Comparable<InfInstant> {
	private final boolean isInfinitePast;
	private final boolean isInfiniteFuture;
	@Nullable
	private final Instant instant;

	public InfInstant(boolean isInfinitePast, boolean isInfiniteFuture, @Nullable Instant instant) {
		if (isInfinitePast && isInfiniteFuture) {
			throw new IllegalArgumentException("InfInstant cannot both be in infinite past and future");
		}
		if (null != instant && (isInfinitePast || isInfiniteFuture)) {
			throw new IllegalArgumentException("InfInstant cannot be at an infinite time and definite time");
		}
		if (null == instant && !(isInfinitePast || isInfiniteFuture)) {
			throw new IllegalArgumentException("Finite InfInstant must have nonnull time");
		}
		if (Instant.MIN.equals(instant) || Instant.MAX.equals(instant)) {
			throw new IllegalArgumentException("Time is min or max");
		}
		this.isInfinitePast = isInfinitePast;
		this.isInfiniteFuture = isInfiniteFuture;
		this.instant = instant;
	}

	public boolean isInfinitePast() {
		return isInfinitePast;
	}

	public boolean isInfiniteFuture() {
		return isInfiniteFuture;
	}

	public Optional<Instant> getInstant() {
		return Optional.ofNullable(instant);
	}

	public Instant getInstantOrElseThrow() {
		return Optional.ofNullable(instant).orElseThrow();
	}

	public boolean isFinite() {
		return !isInfinitePast && !isInfiniteFuture;
	}

	public boolean isBefore(InfInstant time) {
		return this.compareTo(time) < 0;
	}

	public boolean isAfter(InfInstant time) {
		return this.compareTo(time) > 0;
	}

	public InfInstant plusNanos(long nanosToAdd) {
		if (isInfinitePast()) {
			return this;
		} else if (isInfiniteFuture()) {
			return this;
		} else {
			return InfInstant.of(definiteGetInstant().plusNanos(nanosToAdd));
		}
	}

	private @NotNull Instant definiteGetInstant() {
		return getInstant().orElseThrow(() -> new IllegalStateException("Expected definite InfInstant to have instant defined"));
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;

		InfInstant that = (InfInstant) o;
		return isInfinitePast() == that.isInfinitePast() && isInfiniteFuture() == that.isInfiniteFuture() && Objects.equals(
			getInstant(),
			that.getInstant()
		);
	}

	@Override
	public int hashCode() {
		int result = Boolean.hashCode(isInfinitePast());
		result = 31 * result + Boolean.hashCode(isInfiniteFuture());
		result = 31 * result + Objects.hashCode(getInstant());
		return result;
	}

	@Override
	public int compareTo(@NotNull InfInstant o) {
		if (this.isInfinitePast()) {
			if (o.isInfinitePast()) {
				return 0;
			}
			return -1;
		}
		if (this.isInfiniteFuture()) {
			if (o.isInfiniteFuture()) {
				return 0;
			}
			return 1;
		}
		if (o.isInfinitePast()) {
			return 1;
		}
		if (o.isInfiniteFuture()) {
			return -1;
		}
		if (this.getInstant().orElse(null) instanceof Instant thisInstant && o.getInstant().orElse(null) instanceof Instant thatInstant) {
			return thisInstant.compareTo(thatInstant);
		}
		throw new IllegalStateException("Expected definite InfInstant to have instant defined");
	}

	@Override
	public String toString() {
		if (isInfinitePast()) {
			return "InfPast";
		} else if (isInfiniteFuture()) {
			return "InfFuture";
		} else {
			return definiteGetInstant().toString();
		}
	}

	public static InfInstant of(Instant instant) {
		return new InfInstant(false, false, instant);
	}

	public static InfInstant infinitePast() {
		return new InfInstant(true, false, null);
	}

	public static InfInstant infiniteFuture() {
		return new InfInstant(false, true, null);
	}

	public static InfInstant now() {
		return new InfInstant(false, false, Instant.now());
	}
}
