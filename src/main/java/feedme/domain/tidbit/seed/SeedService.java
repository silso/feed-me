package feedme.domain.tidbit.seed;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.*;

public class SeedService {
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private static final int POLL_RATE_MILLISECONDS = 50;

    private final SeedRepository seeds;
	private final Clock clock;

	public SeedService(SeedRepository seeds, Clock clock) {
        this.seeds = seeds;
		this.clock = clock;
	}

    public void start() {
        service.scheduleAtFixedRate(this::poll, 0, POLL_RATE_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    private void poll() {
        seeds.forEach((id, seed) -> {
            Instant now = Instant.now(clock);
            seed.updateTidbits(now);
        });
    }
}
