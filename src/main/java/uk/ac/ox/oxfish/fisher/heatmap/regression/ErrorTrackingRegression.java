package uk.ac.ox.oxfish.fisher.heatmap.regression;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedList;

/**
 * This is a decorator that tracks the error rate of all the regressions
 * Created by carrknight on 8/19/16.
 */
public class ErrorTrackingRegression<V> implements GeographicalRegression<V>
{


    private static final int DEFAULT_MEMORY_SIZE = 20;

    private double latestError = Double.NaN;

    private final  int memorySize;

    private final LinkedList<Double> errors = new LinkedList<>();

    private final GeographicalRegression<V> delegate;


    public ErrorTrackingRegression(
            GeographicalRegression<V> delegate, int memorySize) {
        this.memorySize = memorySize;
        this.delegate = delegate;
    }

    public ErrorTrackingRegression(
            GeographicalRegression<V> delegate) {
        this(delegate,DEFAULT_MEMORY_SIZE);
    }


    @Override
    public double predict(
            SeaTile tile, double time, Fisher fisher, FishState model) {
        return delegate.predict(tile, time, fisher, model);
    }

    @Override
    public void addObservation(
            GeographicalObservation<V> observation, Fisher fisher, FishState model) {
        //compute error (MSE)
        double error = predict(observation.getTile(),observation.getTime(),fisher,model ) -
                extractNumericalYFromObservation(observation,fisher);
        error = error * error;
        //store it
        latestError = error;
        errors.addFirst(error);
        while(errors.size() > memorySize)
        {
            assert  errors.size() == memorySize + 1;
            errors.removeLast();
        }

        //now learn.
        delegate.addObservation(observation,fisher,model );

    }

    /**
     *  returns list of errors. No protection here, be careful
     */
    public LinkedList<Double> getErrors() {
        return errors;
    }


    /**
     * delegated
     */
    @Override
    public void start(FishState model,Fisher fisher) {
        delegate.start(model,fisher);
    }

    /**
     * delegated
     */
    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }
    /**
     * Getter for property 'latestError'.
     *
     * @return Value for property 'latestError'.
     */
    public double getLatestError() {
        return latestError;
    }

    /**
     * Getter for property 'memorySize'.
     *
     * @return Value for property 'memorySize'.
     */
    public int getMemorySize() {
        return memorySize;
    }

    /**
     * It's already a double so return it!
     */
    @Override
    public double extractNumericalYFromObservation(
            GeographicalObservation<V> observation, Fisher fisher) {
        return delegate.extractNumericalYFromObservation(observation,fisher);
    }

    /**
     *  ducked, let delegate handle it
     */
    @Override
    public double[] getParametersAsArray() {
        return delegate.getParametersAsArray();
    }

    /**
     *  ducked, let delegate handle it
     */
    @Override
    public void setParameters(double[] parameterArray) {
        delegate.setParameters(parameterArray);
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public GeographicalRegression<V> getDelegate() {
        return delegate;
    }
}
