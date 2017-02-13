package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * extracts for each observation a random number.
 * Nasty and used only to add noise and make prediction worse
 * Created by carrknight on 7/7/16.
 */
public class RandomObservationExtractor implements ObservationExtractor
{


    private double maxNoise;

    final private MersenneTwisterFast randomizer;


    public RandomObservationExtractor(double maxNoise, MersenneTwisterFast randomizer)
    {
        this.maxNoise = maxNoise;
        this.randomizer = randomizer;
    }


    @Override
    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
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

    /**
     * Setter for property 'maxNoise'.
     *
     * @param maxNoise Value to set for property 'maxNoise'.
     */
    public void setMaxNoise(double maxNoise) {
        this.maxNoise = maxNoise;
    }
}
