package feedme.app.cli.action;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableBiMap;
import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.action.TidbitAction;
import feedme.domain.tidbit.action.TidbitActionException;
import feedme.domain.tidbit.action.impl.ConsumeAction;
import feedme.domain.tidbit.action.impl.EmitAction;
import feedme.domain.tidbit.action.impl.ExpireAction;
import feedme.domain.tidbit.action.impl.OnItAction;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;

public class ActionService {
    private final ImmutableBiMap<String, Class<? extends TidbitAction>> ACTIONS = new ImmutableBiMap.Builder<String, Class<? extends TidbitAction>>()
        .put("emit", EmitAction.class)
        .put("on-it", OnItAction.class)
        .put("consume", ConsumeAction.class)
        .put("expire", ExpireAction.class)
        .build();

    public void applyActionToTidbit(String actionString, Tidbit tidbit) throws ActionException {
        if (!ACTIONS.containsKey(actionString)) {
            throw new ActionException("Action type doesn't exist: '%s'".formatted(actionString));
        }
        Class<? extends TidbitAction> actionClass = ACTIONS.get(actionString);
        if (!tidbit.allowedUserActions().contains(actionClass)) {
            throw new ActionException("Action type not allowed for tidbit: '%s'".formatted(actionClass));
        }
        TidbitAction action;
        try {
            assert actionClass != null;
            action = actionClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ActionException("Failed to instantiate action type: '%s'".formatted(actionClass));
        }
        try {
            action.apply(tidbit, Instant.now());
        } catch (TidbitActionException e) {
            throw new ActionException("Failed to apply action to tidbit");
        }
    }

    public String getAllowedActionsForTidbit(Tidbit tidbit) {
        List<String> actionStrings = tidbit.allowedUserActions().stream().map(action -> ACTIONS.inverse().get(action)).toList();
        return Joiner.on(", ").join(actionStrings);
    }

    public static class ActionException extends Exception {
        private ActionException(String message) {
            super(message);
        }
    }
}
