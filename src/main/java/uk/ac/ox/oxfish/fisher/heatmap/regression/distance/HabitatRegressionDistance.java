package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * distance in rockyness
 * Created by carrknight on 7/7/16.
 */
public class HabitatRegressionDistance implements RegressionDistance {



    private final  double habitatBandwidth;

    public HabitatRegressionDistance(double habitatBandwidth) {
        this.habitatBandwidth = habitatBandwidth;
    }


    @Override
    public double distance(
            Fisher fisher, SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {
        double distance = tile.getRockyPercentage() - observation.getTile().getRockyPercentage();
        return distance*distance/habitatBandwidth;
    }
}
