package feedme.domain.tidbit.seed;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SeedService {
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private static final int POLL_RATE_MILLISECONDS = 5;

    private final SeedRepository seeds;

    public SeedService(SeedRepository seeds) {
        this.seeds = seeds;
    }

    public void start() {
        service.scheduleAtFixedRate(this::poll, 0, POLL_RATE_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    private void poll() {
        seeds.forEach((id, seed) -> {
            Instant now = Instant.now();
            seed.createTidbits(now);
        });
    }
}
