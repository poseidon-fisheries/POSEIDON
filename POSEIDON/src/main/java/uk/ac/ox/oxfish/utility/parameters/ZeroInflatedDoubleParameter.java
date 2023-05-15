package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;

/**
 * A decorator class for {@link DoubleParameter}, which adds a probability that
 * the parameter will take a value of zero instead of its normal value.
 */
public class ZeroInflatedDoubleParameter implements DoubleParameter {

    private DoubleParameter delegate;
    private double proportionOfZeros = 0;

    public ZeroInflatedDoubleParameter() {
    }

    public ZeroInflatedDoubleParameter(final DoubleParameter delegate, final double proportionOfZeros) {
        this.delegate = delegate;
        this.proportionOfZeros = proportionOfZeros;
    }

    @Override
    public DoubleParameter makeCopy() {
        return new ZeroInflatedDoubleParameter(getDelegate(), getProportionOfZeros());
    }

    public DoubleParameter getDelegate() {
        return delegate;
    }

    public void setDelegate(final DoubleParameter delegate) {
        this.delegate = delegate;
    }

    public double getProportionOfZeros() {
        return proportionOfZeros;
    }

    public void setProportionOfZeros(final double proportionOfZeros) {
        this.proportionOfZeros = proportionOfZeros;
    }

    @Override
    public double applyAsDouble(final MersenneTwisterFast rng) {
        return rng.nextDouble() < proportionOfZeros
            ? 0
            : delegate.applyAsDouble(rng);
    }
}
