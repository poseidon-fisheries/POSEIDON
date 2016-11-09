package uk.ac.ox.oxfish.model.data;

import com.google.common.base.Preconditions;

/**
 * EMA: each observation makes average = (1-alpha)average+(alpha)*new_observation
 * Created by carrknight on 11/9/16.
 */
public class ExponentialMovingAverage<T extends Number> implements Averager<T> {


    private double average = Double.NaN;

    private final double alpha;

    public ExponentialMovingAverage(double alpha) {
        this.alpha = alpha;
        Preconditions.checkArgument(alpha>=0);
        Preconditions.checkArgument(alpha<=1);
    }


    @Override
    public void addObservation(T observation) {
        if(Double.isFinite(average))
            average = (1-alpha)*average + alpha*observation.doubleValue();
        else
            average=observation.doubleValue();
    }

    /**
     * returns the average
     */
    @Override
    public double getSmoothedObservation() {
        return average;
    }
}
