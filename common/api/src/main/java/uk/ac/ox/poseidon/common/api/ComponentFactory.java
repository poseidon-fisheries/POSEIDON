package uk.ac.ox.poseidon.common.api;

import sim.engine.SimState;

import java.util.function.Function;

public interface ComponentFactory<S extends SimState, T> extends Function<S, T> {
}
