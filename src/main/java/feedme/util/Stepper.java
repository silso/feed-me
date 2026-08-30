package feedme.util;

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class Stepper<T> {
	@NotNull
	private final Iterator<T> iterator;
	private T value;
	private boolean shouldStep = true;

	public Stepper(@NotNull Iterator<T> iterator) {
		this.iterator = iterator;
	}

	public boolean step() {
		if (!shouldStep) {
			return false;
		}
		shouldStep = false;
		if (iterator.hasNext()) {
			value = iterator.next();
			return true;
		}
		return false;
	}

	public void shouldStep() {
		shouldStep = true;
	}

	public T get() {
		return value;
	}
}
