package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Time distance in hours. This is not squared
 * Created by carrknight on 7/7/16.
 */
public class TimeRegressionDistance implements RegressionDistance
{


    final private double timeBandwidth;

    public TimeRegressionDistance(double timeBandwidth) {
        this.timeBandwidth = timeBandwidth;
    }

    @Override
    public double distance(
            Fisher fisher, SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {
        return Math.abs(currentTimeInHours-observation.getTime())/timeBandwidth;
    }


    /**
     * Getter for property 'timeBandwidth'.
     *
     * @return Value for property 'timeBandwidth'.
     */
    public double getTimeBandwidth() {
        return timeBandwidth;
    }
}
