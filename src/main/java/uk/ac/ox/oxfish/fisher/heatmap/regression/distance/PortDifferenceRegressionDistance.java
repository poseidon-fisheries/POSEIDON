package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Differences in distance from port is what defines this distance
 * Created by carrknight on 7/7/16.
 */
public class PortDifferenceRegressionDistance implements RegressionDistance {


    private final double bandwidth;


    public PortDifferenceRegressionDistance(double bandwidth)
    {
        this.bandwidth = bandwidth;
    }

    @Override
    public double distance(
            Fisher fisher, SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {

        SeaTile portLocation = fisher.getHomePort().getLocation();
        double distanceOne = SpaceRegressionDistance.cellDistance(portLocation,tile);
        double distanceTwo = SpaceRegressionDistance.cellDistance(portLocation,observation.getTile());
        double distance = Math.abs(distanceOne-distanceTwo);
        return distance/bandwidth;





    }

    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public double getBandwidth() {
        return bandwidth;
    }
}
