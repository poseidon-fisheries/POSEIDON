package uk.ac.ox.oxfish.fisher.selfanalysis.hidden;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * The markov evidence, relating (at least for now) to a catch (or better a proportion of total biomass available)
 * Created by carrknight on 6/27/16.
 */
public class MarkovEvidence {

    private final SeaTile tile;

    private final double observation;


    public MarkovEvidence(SeaTile tile, double observation)
    {

        Preconditions.checkArgument(observation<=1);
        Preconditions.checkArgument(observation>0);
        this.tile = tile;
        this.observation = observation;
    }

    public MarkovEvidence(SeaTile tile, double biomassObserved, double totalBiomassAvailable)
    {


        this.tile = tile;
        this.observation = biomassObserved/totalBiomassAvailable;
    }

    /**
     * Getter for property 'tile'.
     *
     * @return Value for property 'tile'.
     */
    public SeaTile getTile() {
        return tile;
    }

    /**
     * Getter for property 'observation'.
     *
     * @return Value for property 'observation'.
     */
    public double getObservation() {
        return observation;
    }
}
