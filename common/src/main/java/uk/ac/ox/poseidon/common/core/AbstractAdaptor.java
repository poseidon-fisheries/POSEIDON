package uk.ac.ox.poseidon.common.core;

public abstract class AbstractAdaptor<D> implements uk.ac.ox.poseidon.common.api.Adaptor<D> {
    private final D delegate;

    protected AbstractAdaptor(final D delegate) {
        this.delegate = delegate;
    }

    @Override
    public D getDelegate() {
        return delegate;
    }

}
