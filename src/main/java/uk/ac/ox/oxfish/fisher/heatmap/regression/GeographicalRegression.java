package uk.ac.ox.oxfish.fisher.heatmap.regression;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Anything that predicts over a geographical map
 * Created by carrknight on 6/27/16.
 */
public interface GeographicalRegression
{


    public double predict(SeaTile tile, double time, FishState state);

    public void  addObservation(GeographicalObservation observation);


}
