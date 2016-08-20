package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.Startable;

/**
 * Anything that predicts over a geographical map
 * Created by carrknight on 6/27/16.
 */
public interface GeographicalRegression<V> extends Startable
{


    /**
     * predict numerical value here
     * @param tile
     * @param time
     * @param fisher
     * @return
     */
    public double predict(SeaTile tile, double time, Fisher fisher);

    /**
     * learn from this observation
     * @param observation
     * @param fisher
     */
    public void  addObservation(GeographicalObservation<V> observation, Fisher fisher);

    /**
     * turn the "V" value of the geographical observation into a number
     * @param observation
     * @param fisher
     * @return
     */
    public double extractNumericalYFromObservation(GeographicalObservation<V> observation, Fisher fisher);

}
