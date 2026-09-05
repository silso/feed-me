package feedme.domain.schedule.timeset;

import feedme.domain.schedule.timeset.operation.TimeSetBinaryOperationVisitor;
import feedme.domain.schedule.timeset.operation.TimeSetUnaryOperationVisitor;
import feedme.util.InfInstant;
import java.time.Duration;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class EmptyTimeSet implements MeasurableTimeSet {
	@Override
	public Optional<TimeSpan> getPrevious(InfInstant time) {
		return Optional.empty();
	}

	@Override
	public Optional<TimeSpan> getAt(InfInstant time) {
		return Optional.empty();
	}

	@Override
	public Optional<TimeSpan> getNext(InfInstant time) {
		return Optional.empty();
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
	public Optional<TimeSpan> getFirst() {
		return Optional.empty();
	}

	@Override
	public Optional<TimeSpan> getLast() {
		return Optional.empty();
	}

	@Override
	public Optional<TimeSegment> measurableGetFirst() {
		return Optional.empty();
	}

	@Override
	public Optional<TimeSegment> measurableGetLast() {
		return Optional.empty();
	}

	@Override
	public Duration getDuration() {
		return Duration.ZERO;
	}

	@Override
	public EverythingTimeSet invert() {
		return EverythingTimeSet.get();
	}

	private static final EmptyTimeSet INSTANCE = new EmptyTimeSet();

	public static EmptyTimeSet get() {
		return INSTANCE;
	}
}
