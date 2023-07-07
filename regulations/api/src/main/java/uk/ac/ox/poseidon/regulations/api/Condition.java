package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;

@FunctionalInterface
public interface Condition extends Predicate<Action> {
}
