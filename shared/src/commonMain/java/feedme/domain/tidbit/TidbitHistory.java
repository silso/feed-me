package feedme.domain.tidbit;

import feedme.domain.tidbit.action.TidbitAction;

import java.util.List;

public record TidbitHistory(
    List<TidbitAction> actions
) {
}
