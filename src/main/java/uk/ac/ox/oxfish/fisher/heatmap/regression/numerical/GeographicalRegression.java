package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * Anything that predicts over a geographical map
 * Created by carrknight on 6/27/16.
 */
public interface GeographicalRegression<V> extends FisherStartable
{


    /**
     * predict numerical value here
     * @param tile
     * @param time
     * @param fisher
     * @param model
     * @return
     */
    public double predict(SeaTile tile, double time, Fisher fisher, FishState model);

    /**
     * learn from this observation
     * @param observation
     * @param fisher
     * @param model
     */
    public void  addObservation(GeographicalObservation<V> observation, Fisher fisher, FishState model);

    /**
     * turn the "V" value of the geographical observation into a number
     * @param observation
     * @param fisher
     * @return
     */
    public double extractNumericalYFromObservation(GeographicalObservation<V> observation, Fisher fisher);

    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     * @return an array containing all the parameters of the model
     */
    public double[] getParametersAsArray();

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     * @param parameterArray the new parameters for this regresssion
     */
    public void setParameters(double[] parameterArray);

}
