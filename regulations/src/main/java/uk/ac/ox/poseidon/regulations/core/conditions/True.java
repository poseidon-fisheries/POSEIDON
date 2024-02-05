package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

public enum True implements Condition {

    TRUE;

    @Override
    public boolean test(final Action action) {
        return true;
    }
}
