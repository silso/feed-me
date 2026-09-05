package feedme.domain.schedule.timeset;

import feedme.domain.schedule.timeset.operation.TimeSetBinaryOperationVisitor;
import feedme.domain.schedule.timeset.operation.TimeSetUnaryOperationVisitor;
import feedme.util.InfInstant;
import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class EverythingTimeSet implements TimeSet {
	@Override
	public Optional<TimeSpan> getPrevious(@NotNull Instant time) throws TimeSetException.Unchecked {
		return Optional.of(EverythingTimeSpan.get());
	}

	@Override
	public Optional<TimeSpan> getAt(@NotNull Instant time) throws TimeSetException.Unchecked {
		return Optional.of(EverythingTimeSpan.get());
	}

	@Override
	public Optional<TimeSpan> getNext(@NotNull Instant time) throws TimeSetException.Unchecked {
		return Optional.of(EverythingTimeSpan.get());
	}

	@Override
	public @NotNull TimeSet accept(TimeSetBinaryOperationVisitor visitor, TimeSet other) {
		return visitor.visit(other, this);
	}

	@Override
	public @NotNull TimeSet accept(TimeSetUnaryOperationVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	private static final EverythingTimeSet INSTANCE = new EverythingTimeSet();

	public static EverythingTimeSet get() {
		return INSTANCE;
	}

	private static class EverythingTimeSpan extends TimeSpan {
		private EverythingTimeSpan() {
			super(InfInstant.infinitePast(), InfInstant.infiniteFuture());
		}

		private static EverythingTimeSpan get() {
			return new EverythingTimeSpan();
		}
	}
}
