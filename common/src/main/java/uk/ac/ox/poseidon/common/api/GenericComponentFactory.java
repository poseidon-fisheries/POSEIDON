package uk.ac.ox.poseidon.common.api;

import java.util.function.Function;

public interface GenericComponentFactory<S extends ModelState, T> extends Function<S, T> {
}
