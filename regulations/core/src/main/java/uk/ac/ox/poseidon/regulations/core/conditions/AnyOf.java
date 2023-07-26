package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Collection;
import java.util.Set;

public class AnyOf implements Condition {

    private final Set<Condition> conditions;

    public AnyOf(final Collection<Condition> conditions) {
        this.conditions = ImmutableSet.copyOf(conditions);
    }

    public Set<Condition> getConditions() {
        return conditions;
    }

    @Override
    public boolean test(final Action action) {
        return conditions.stream().anyMatch(predicate -> predicate.test(action));
    }
}