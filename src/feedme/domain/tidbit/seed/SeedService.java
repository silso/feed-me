package feedme.domain.tidbit.seed;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SeedService {
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private static final int POLL_RATE_SECONDS = 60;

    private final SeedRepository seeds;

    public SeedService(SeedRepository seeds) {
        this.seeds = seeds;
    }

    public void start() {
        service.schedule(this::poll, POLL_RATE_SECONDS, TimeUnit.SECONDS);
    }

    private void poll() {
        seeds.forEach((id, seed) -> {
            Instant now = Instant.now();
            seed.createTidbits(now);
        });
    }
}
