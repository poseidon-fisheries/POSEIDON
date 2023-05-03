package uk.ac.ox.poseidon.common;

import java.util.function.Function;

public interface AdaptorFactory<D, T> extends Function<D, T> {
    static <D, T, F extends AdaptorFactory<D, T>> F loadFor(
        final Class<F> adaptorFactoryClass,
        final Class<D> delegateClass
    ) {
        final F adaptorFactory = Services.loadFirst(
            adaptorFactoryClass,
            f -> f.getDelegateClass().isAssignableFrom(delegateClass)
        );
        return adaptorFactoryClass.cast(adaptorFactory);
    }

    Class<D> getDelegateClass();
}
