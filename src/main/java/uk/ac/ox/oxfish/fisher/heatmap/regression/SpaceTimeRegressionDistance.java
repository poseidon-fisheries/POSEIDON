package uk.ac.ox.oxfish.fisher.heatmap.regression;

import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Created by carrknight on 7/5/16.
 */
public class SpaceTimeRegressionDistance implements RegressionDistance
{


    /**
     * the time bandwidth
     */
    private final double timeBandwidth;

    /**
     * the space bandwidth
     */
    private final double spaceBandwidth;


    public SpaceTimeRegressionDistance(double timeBandwidth,
                                       double spaceBandwidth) {
        this.timeBandwidth = timeBandwidth;
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

        final double timeDistance = Math.abs(currentTimeInHours-observation.getTime());
        distance += (timeDistance)/timeBandwidth;
        return distance;
    }
}
