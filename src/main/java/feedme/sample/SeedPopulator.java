package feedme.sample;

import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.seed.SeedRepository;

public final class SeedPopulator implements Populator<SeedRepository> {
    private final TidbitRepository tidbitRepository;

    public SeedPopulator(TidbitRepository tidbitRepository) {
        this.tidbitRepository = tidbitRepository;
    }

    @Override
    public void populate(SeedRepository repository) {
    }
}
