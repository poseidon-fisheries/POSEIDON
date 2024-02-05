package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.ToDoubleFunction;

@FunctionalInterface
public interface Quantity extends ToDoubleFunction<Action> {
}
