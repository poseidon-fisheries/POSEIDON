package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.poseidon.agents.api.Action;

import java.util.Collection;
import java.util.Set;

public class AllOf implements Condition {

    private final Set<Condition> conditions;

    public AllOf(final Collection<Condition> conditions) {
        this.conditions = ImmutableSet.copyOf(conditions);
    }

    @Override
    public boolean test(final Action action) {
        return conditions.stream().allMatch(predicate -> predicate.test(action));
    }
}
