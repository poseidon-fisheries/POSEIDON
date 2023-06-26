package uk.ac.ox.poseidon.common.api;

import java.util.function.Function;

public interface AdaptorFactory<D, T> extends Function<D, T> {
    Class<D> getDelegateClass();
}
