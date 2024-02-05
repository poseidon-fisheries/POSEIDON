package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

@FunctionalInterface
public interface Condition extends Predicate<Action> {
    default Set<Condition> getSubConditions() {
        return Collections.emptySet();
    }
}
