package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;

/**
 * The parameter returned is always the same
 * Created by carrknight on 6/7/15.
 */
public class FixedDoubleParameter implements DoubleParameter {

    private double fixedValue;

    public FixedDoubleParameter() {
    }

    public FixedDoubleParameter(double fixedValue) {
        this.fixedValue = fixedValue;
    }


    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public Double apply(MersenneTwisterFast mersenneTwisterFast) {
        return fixedValue;
    }

    public double getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(double fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public FixedDoubleParameter makeCopy() {
        return new FixedDoubleParameter(fixedValue);
    }
}
