package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * An object that is supposed to track a value in order to predict it
 * Created by carrknight on 8/18/15.
 */
public interface Predictor extends FisherStartable{

    /**
     * ask the predictor the expected value of the variable it is tracking
     * @return the expected value
     */
    public double predict();

    /**
     * ask the predictor what is the probability the variable it is tracking is below a given level
     * @param level the level
     * @return P(x < level)
     */
    public double probabilityBelowThis(double level);



}
