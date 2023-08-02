package feedme.domain.tidbit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TidbitService {
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private static final int POLL_RATE_SECONDS = 60;

    public void start() {
        service.schedule(this::poll, POLL_RATE_SECONDS, TimeUnit.SECONDS);
    }

    private void poll() {

    }
}
