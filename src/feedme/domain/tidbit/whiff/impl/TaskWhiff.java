package feedme.domain.tidbit.whiff.impl;

import feedme.domain.tidbit.whiff.Whiff;
import feedme.domain.tidbit.whiff.action.WhiffAction;

import java.util.Set;

import static feedme.domain.tidbit.whiff.action.WhiffAction.Impl.*;

public record TaskWhiff(String message, Whiff.State currentState) implements Whiff {

    public String message() {
        return message;
    }

    @Override
    public Set<WhiffAction.Impl> availableActions() {
        return Set.of(OnItWhiffAction, ConsumeWhiffAction);
    }
}
