package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * space only regression distance
 * Created by carrknight on 7/7/16.
 */
public class SpaceRegressionDistance  implements RegressionDistance{


    final public double spaceBandwidth;


    public SpaceRegressionDistance(double spaceBandwidth) {
        this.spaceBandwidth = spaceBandwidth;
    }

    @Override
    public double distance(
            Fisher fisher, SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {

        return cellDistance(tile,observation.getTile())/spaceBandwidth;
    }


    /**
     * Getter for property 'spaceBandwidth'.
     *
     * @return Value for property 'spaceBandwidth'.
     */
    public double getSpaceBandwidth() {
        return spaceBandwidth;
    }


    final public static double cellDistance(SeaTile first, SeaTile second)
    {
        double distance = 0;
        double spaceDistance = first.getGridX() - second.getGridX();
        distance += (spaceDistance*spaceDistance);
        spaceDistance = first.getGridY() - second.getGridY();
        distance += (spaceDistance*spaceDistance);
        return distance;
    }

}
