package uk.ac.ox.poseidon.common.core.temporal;

import java.time.temporal.TemporalAccessor;

/**
 * A {@link TemporalMap} that always returns the same object, no matter what date it is queried at.
 *
 * @param <T> The type of the stored object.
 */
public class ConstantTemporalMap<T> implements TemporalMap<T> {
    private final T object;

    public ConstantTemporalMap(final T object) {this.object = object;}

    @Override
    public T get(final TemporalAccessor temporal) {
        return object;
    }
}
