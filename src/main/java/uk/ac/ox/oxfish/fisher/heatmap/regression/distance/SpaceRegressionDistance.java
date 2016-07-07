package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

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
            SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {
        double distance = 0;
        double spaceDistance = tile.getGridX() - observation.getTile().getGridX();
        if (spaceDistance != 0)
            distance += (spaceDistance*spaceDistance)/spaceBandwidth;
        spaceDistance = tile.getGridY() - observation.getTile().getGridY();
        if (spaceDistance != 0)
            distance += (spaceDistance*spaceDistance)/spaceBandwidth;
        return distance;
    }


    /**
     * Getter for property 'spaceBandwidth'.
     *
     * @return Value for property 'spaceBandwidth'.
     */
    public double getSpaceBandwidth() {
        return spaceBandwidth;
    }
}
