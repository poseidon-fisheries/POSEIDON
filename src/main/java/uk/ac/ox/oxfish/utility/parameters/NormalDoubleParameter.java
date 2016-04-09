package uk.ac.ox.oxfish.utility.parameters;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;

/**
 * Returns a gaussian double value randomly
 *
 * Created by carrknight on 6/7/15.
 */
public class NormalDoubleParameter implements DoubleParameter {


    private double mean;

    private double standardDeviation;

    public NormalDoubleParameter() {
    }

    public NormalDoubleParameter(double mean, double standardDeviation) {
        Preconditions.checkArgument(standardDeviation >=0);
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public Double apply(MersenneTwisterFast mersenneTwisterFast) {
        return mersenneTwisterFast.nextGaussian()*standardDeviation + mean;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    @Override
    public DoubleParameter makeCopy() {
        return new NormalDoubleParameter(mean,standardDeviation);
    }
}
