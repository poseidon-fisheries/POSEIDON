package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;

/**
 * always returns NaN
 * Created by carrknight on 5/25/17.
 */
public class NullParameter implements DoubleParameter {
    @Override
    public DoubleParameter makeCopy() {
        return new NullParameter();
    }

    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public Double apply(MersenneTwisterFast mersenneTwisterFast) {
        return Double.NaN;
    }
}
