package feedme.domain.tidbit.action;

import feedme.domain.tidbit.TidbitState;

public interface TidbitStateTransition extends TidbitAction {
    TidbitState toState();
}
