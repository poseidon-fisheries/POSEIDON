package uk.ac.ox.poseidon.common.core.temporal;

import java.time.temporal.TemporalAccessor;

/**
 * A container that returns an object based on the requested step.
 *
 * @param <V> the type of the object to return
 */
@FunctionalInterface
public interface TemporalMap<V> {
    V get(TemporalAccessor temporal);
}
