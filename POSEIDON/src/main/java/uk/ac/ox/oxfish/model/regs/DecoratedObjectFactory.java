package uk.ac.ox.oxfish.model.regs;

public abstract class DecoratedObjectFactory<T> {
    private T delegate;

    public DecoratedObjectFactory() {
    }

    public DecoratedObjectFactory(final T delegate) {
        this.delegate = delegate;
    }

    public T getDelegate() {
        return delegate;
    }

    public void setDelegate(final T delegate) {
        this.delegate = delegate;
    }
}
