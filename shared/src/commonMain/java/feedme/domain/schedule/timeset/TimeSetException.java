package feedme.domain.schedule.timeset;

public class TimeSetException extends Exception {
    public TimeSetException(String message) {
        super(message);
    }

    public Unchecked unchecked() {
        return new Unchecked(this);
    }

    public static <T> T unchecked(ThrowsTimeSetExceptionCallable<T> callable) throws Unchecked {
        try {
            return callable.call();
        } catch (TimeSetException e) {
            throw e.unchecked();
        }
    }

    public static class Unchecked extends RuntimeException {
        private final TimeSetException e;
        private Unchecked(TimeSetException e) {
            this.e = e;
        }

        public TimeSetException checked() {
            return e;
        }
    }

    @FunctionalInterface
    public interface ThrowsTimeSetExceptionCallable<T> {
        T call() throws TimeSetException;
    }
}
