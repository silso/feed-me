package feedme.domain.tidbit.action.impl;

import feedme.domain.tidbit.task.TaskTidbit;
import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.TidbitState;
import feedme.domain.tidbit.action.TidbitStateTransition;

public class OnItAction extends TidbitStateTransition {
    @Override
    public boolean doIsApplicableTo(Tidbit tidbit) {
        return !TaskTidbit.State.OnIt.equals(tidbit.currentState) && !tidbit.currentState.isFinished();
    }

    @Override
    public TidbitState toState() {
        return TaskTidbit.State.OnIt;
    }
}
