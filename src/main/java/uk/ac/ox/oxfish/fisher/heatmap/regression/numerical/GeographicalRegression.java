package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

import java.util.Collection;
import java.util.HashMap;

/**
 * Anything that predicts over a geographical map
 * Created by carrknight on 6/27/16.
 */
public interface GeographicalRegression<V> extends Startable
{


    public double predict(SeaTile tile, double time, FishState state, Fisher fisher);

    public void  addObservation(GeographicalObservation<V> observation, Fisher fisher);


}
