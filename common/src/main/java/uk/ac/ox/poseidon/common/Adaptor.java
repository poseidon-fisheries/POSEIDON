package uk.ac.ox.poseidon.common;

public abstract class Adaptor<D> {
    private final D delegate;

    protected Adaptor(final D delegate) {
        this.delegate = delegate;
    }

    public D getDelegate() {
        return delegate;
    }

}
