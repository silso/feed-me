package feedme.util;

import java.time.*;

public class CreativeClock extends Clock {
	private final ZoneId zone;
	private final Instant anchor;

	private long multiplier = 1;
	private Duration adjustmentOffset = Duration.ZERO;
	private Duration offset = Duration.ZERO;

	public CreativeClock() {
		this(ZoneId.systemDefault());
	}

	public CreativeClock(ZoneId zone) {
		this.zone = zone;
		this.anchor = Instant.now();
	}

	private CreativeClock(ZoneId zone, CreativeClock other) {
		this.zone = zone;
		this.anchor = other.anchor;
		this.multiplier = other.multiplier;
		this.adjustmentOffset = other.adjustmentOffset;
		this.offset = other.offset;
	}

	@Override
	public ZoneId getZone() {
		return zone;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return new CreativeClock(zone, this);
	}

	@Override
	public Instant instant() {
		var now = Duration.between(anchor, Instant.now());
		return anchor.plus(now.plus(adjustmentOffset).multipliedBy(multiplier).plus(offset));
	}

	public void setMultiplier(long newMultiplier) {
		var now = Duration.between(anchor, Instant.now());
		adjustmentOffset = now.plus(adjustmentOffset).multipliedBy(multiplier).dividedBy(newMultiplier).minus(now);
		this.multiplier = newMultiplier;
	}

	public void setOffset(Duration offset) {
		this.offset = offset;
	}

	public void addOffset(Duration offset) {
		this.offset = this.offset.plus(offset);
	}
}
