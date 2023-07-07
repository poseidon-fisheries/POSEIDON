package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;

import static com.google.common.base.Preconditions.checkNotNull;

public class Not implements Condition {

    private final Condition condition;

    public Not(final Condition condition) {
        this.condition = checkNotNull(condition);
    }

    @Override
    public boolean test(final Action action) {
        return !condition.test(action);
    }
}
