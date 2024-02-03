package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

/**
 * A decorator class for {@link DoubleParameter}, which adds a scaling factor
 * by which the parameter value is multiplied.
 */
public class ScaledDoubleParameter implements DoubleParameter {

    private DoubleParameter delegate;
    private double scalingFactor = 1;

    public ScaledDoubleParameter() {
    }

    public ScaledDoubleParameter(final DoubleParameter delegate, final double scalingFactor) {
        this.delegate = delegate;
        this.scalingFactor = scalingFactor;
    }

    @Override
    public DoubleParameter makeCopy() {
        return new ScaledDoubleParameter(getDelegate(), getScalingFactor());
    }

    public DoubleParameter getDelegate() {
        return delegate;
    }

    public void setDelegate(final DoubleParameter delegate) {
        this.delegate = delegate;
    }

    public double getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(final double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    @Override
    public double applyAsDouble(final MersenneTwisterFast rng) {
        return delegate.applyAsDouble(rng) * getScalingFactor();
    }
}
