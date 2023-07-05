package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.poseidon.agents.api.Action;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public class AnyOf implements Predicate<Action> {

    private final Set<Predicate<Action>> predicates;

    public AnyOf(final Collection<Predicate<Action>> predicates) {
        this.predicates = ImmutableSet.copyOf(predicates);
    }

    @Override
    public boolean test(final Action action) {
        return predicates.stream().anyMatch(predicate -> predicate.test(action));
    }
}