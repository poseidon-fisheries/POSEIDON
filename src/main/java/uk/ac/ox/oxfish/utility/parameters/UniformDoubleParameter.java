package uk.ac.ox.oxfish.utility.parameters;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;

/**
 * A uniformly distributed double will be returned each time
 *
 * Created by carrknight on 6/7/15.
 */
public class UniformDoubleParameter implements DoubleParameter {

    private double minimum;

    private double maximum;

    public UniformDoubleParameter() {
    }

    public UniformDoubleParameter(double minimum, double maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }


    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public Double apply(MersenneTwisterFast mersenneTwisterFast) {
        Preconditions.checkArgument(maximum >= minimum, "maximum is not bigger than minimum");
        return (maximum-minimum)*mersenneTwisterFast.nextDouble(true,true)+minimum;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }
}
