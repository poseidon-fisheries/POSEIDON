package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Needed to tell the difference between a seatile and a  Geographical Observation
 *
 * Created by carrknight on 7/5/16.
 */
public interface RegressionDistance {

    public double distance(SeaTile tile, double currentTimeInHours,
                           GeographicalObservation observation);


}
