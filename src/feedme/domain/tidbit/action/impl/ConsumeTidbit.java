package feedme.domain.tidbit.action.impl;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.TidbitHistory;
import feedme.domain.tidbit.TidbitState;
import feedme.domain.tidbit.action.TidbitAction;
import feedme.domain.tidbit.action.TidbitStateTransition;

import java.time.Instant;
import java.util.List;

public record ConsumeTidbit(Instant occurredAt) implements TidbitStateTransition {
    public ConsumeTidbit() {
        this(Instant.now());
    }

    @Override
    public boolean isApplicableTo(Tidbit tidbit) {
        return tidbit.currentState() != TidbitState.Consumed;
    }

    @Override
    public Tidbit apply(Tidbit tidbit) {
        List<TidbitAction> newHistory = tidbit.history().actions();
        newHistory.add(new ConsumeTidbit(Instant.now()));
        return new Tidbit(tidbit.createdAt(), TidbitState.Consumed, new TidbitHistory(newHistory), tidbit.message(), tidbit.whiffs(), tidbit.plan());
    }

    @Override
    public TidbitState toState() {
        return TidbitState.Consumed;
    }
}
