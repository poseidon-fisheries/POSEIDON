package uk.ac.ox.oxfish.utility.adaptation.probability;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Probability is fixed and never changes (unless set exogenously)
 * Created by carrknight on 8/28/15.
 */
public class FixedProbability implements AdaptationProbability {



    private double explorationProbability;

    private double imitationProbability;


    public FixedProbability(double explorationProbability, double imitationProbability) {
        Preconditions.checkArgument(explorationProbability <= 1);
        Preconditions.checkArgument(imitationProbability <= 1);
        Preconditions.checkArgument(explorationProbability >= 0);
        Preconditions.checkArgument(imitationProbability >= 0 );
        this.explorationProbability = explorationProbability;
        this.imitationProbability = imitationProbability;
    }


    public double getExplorationProbability() {

        return explorationProbability;
    }

    @Override
    public double getImitationProbability() {

        return imitationProbability;
    }

    @Override
    public void judgeExploration(double previousFitness, double currentFitness) {

    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    /**
     * ignored
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {

    }

    public void setExplorationProbability(double explorationProbability) {
        this.explorationProbability = explorationProbability;
    }

    public void setImitationProbability(double imitationProbability) {
        this.imitationProbability = imitationProbability;
    }
}
