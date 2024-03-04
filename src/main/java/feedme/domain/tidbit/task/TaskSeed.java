package feedme.domain.tidbit.task;

import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.seed.Seed;

import java.time.Instant;

public abstract class TaskSeed extends Seed {
    public final String instruction;
    public final Instant expiresAt;
    public final TaskPriority priority;
    protected final TidbitRepository repository;

    public TaskSeed(TidbitRepository repository, String instruction, Instant expiresAt) {
        this.repository = repository;
        this.instruction = instruction;
        this.expiresAt = expiresAt;
        this.priority = TaskPriority.Minor;
    }
}
