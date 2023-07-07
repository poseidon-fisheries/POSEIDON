package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActionCodeIs implements Condition {
    private final String actionCode;

    public ActionCodeIs(final String actionCode) {
        this.actionCode = checkNotNull(actionCode);
    }

    @Override
    public boolean test(final Action action) {
        return actionCode.equals(action.getCode());
    }
}
