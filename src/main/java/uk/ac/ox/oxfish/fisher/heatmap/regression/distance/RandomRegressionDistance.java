package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * distance is a random number. Nasty and used only to add noise and make prediction worse
 * Created by carrknight on 7/7/16.
 */
public class RandomRegressionDistance implements RegressionDistance
{


    final private double maxNoise;

    final private MersenneTwisterFast randomizer;


    public RandomRegressionDistance(double maxNoise, MersenneTwisterFast randomizer)
    {
        this.maxNoise = maxNoise;
        this.randomizer = randomizer;
    }

    @Override
    public double distance(
            SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {
        return randomizer.nextDouble()*maxNoise;
    }

    /**
     * Getter for property 'maxNoise'.
     *
     * @return Value for property 'maxNoise'.
     */
    public double getMaxNoise() {
        return maxNoise;
    }


}
