package feedme.domain.tidbit.action;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.TidbitState;

public abstract class TidbitStateTransition extends TidbitAction {
    public abstract TidbitState toState();

    @Override
    public <TidbitType extends Tidbit> TidbitType doApply(TidbitType tidbit) {
        // TODO: interact with seeds when applying some actions
        tidbit.currentState = this.toState();
        return tidbit;
    }
}
