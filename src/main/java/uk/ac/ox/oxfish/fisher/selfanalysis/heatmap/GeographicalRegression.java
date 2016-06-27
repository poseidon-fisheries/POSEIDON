package uk.ac.ox.oxfish.fisher.selfanalysis.heatmap;

import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Anything that predicts over a geographical map
 * Created by carrknight on 6/27/16.
 */
public interface GeographicalRegression
{


    public double predict(SeaTile tile, double time);

    public double predict(int x, int y,double time);

    public void  addObservation(GeographicalObservation observation);

}
