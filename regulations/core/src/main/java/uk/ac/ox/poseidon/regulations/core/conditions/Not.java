package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class Not implements Predicate<Action> {

    private final Predicate<? super Action> condition;

    public Not(final Predicate<? super Action> condition) {
        this.condition = checkNotNull(condition);
    }

    @Override
    public boolean test(final Action action) {
        return !condition.test(action);
    }
}
