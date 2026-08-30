package feedme.domain.schedule.timeset;

import java.util.*;
import org.jetbrains.annotations.NotNull;

public abstract class CompositeTimeSet implements TimeSet {
	protected static final long ITERATION_LIMIT = 10_000;

	protected final List<TimeSet> sets;

	protected CompositeTimeSet(List<TimeSet> sets) {
		this.sets = Objects.requireNonNullElse(sets, new ArrayList<>());
		if (sets.size() < 2) {
			throw new IllegalArgumentException("Must be at least 2 time sets in a composite time set");
		}
	}

	protected @NotNull List<TimeSet> getSets() {
		return sets;
	}
}
