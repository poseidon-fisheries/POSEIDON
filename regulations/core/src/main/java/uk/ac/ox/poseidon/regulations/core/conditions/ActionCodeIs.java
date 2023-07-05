package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActionCodeIs implements Predicate<Action> {
    private final String actionCode;

    public ActionCodeIs(final String actionCode) {
        this.actionCode = checkNotNull(actionCode);
    }
    
    @Override
    public boolean test(final Action action) {
        return actionCode.equals(action.getCode());
    }
}
