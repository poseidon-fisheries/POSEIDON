package uk.ac.ox.oxfish.utility.adaptation.probability;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * The probability of exploring increases whenever an exploration is successful and decreases otherwise
 * Created by carrknight on 8/28/15.
 */
public class ExplorationPenaltyProbability implements AdaptationProbability
{


    /**
     * whenever the exploration succeeds, exploration probability is multiplied by by 1+incrementMultiplier. Whenever exploration
     * fails the exploration probability decreases by 1-incrementMultiplier
     */
    private double incrementMultiplier;


    private double explorationMinimum;

    private final FixedProbability delegate;


    public ExplorationPenaltyProbability(
            double explorationProbability, double imitationProbability,
            double incrementMultiplier, double explorationMinimum) {
        this.delegate = new FixedProbability(explorationProbability, imitationProbability);
        this.incrementMultiplier = incrementMultiplier;
        this.explorationMinimum = explorationMinimum;
    }

    public void setExplorationProbability(double explorationProbability) {
        delegate.setExplorationProbability(explorationProbability);
    }

    public void setImitationProbability(double imitationProbability) {
        delegate.setImitationProbability(imitationProbability);
    }

    @Override
    public double getExplorationProbability() {
        return delegate.getExplorationProbability();
    }

    @Override
    public double getImitationProbability() {
        return delegate.getImitationProbability();
    }

    /**
     * ignored
     * @param model
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model,fisher);
    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
    }

    @Override
    public void judgeExploration(double previousFitness, double currentFitness) {
        if(currentFitness  > previousFitness + FishStateUtilities.EPSILON)
            delegate.setExplorationProbability(Math.min(delegate.getExplorationProbability() * (1d+incrementMultiplier),1));
        if( currentFitness < previousFitness - FishStateUtilities.EPSILON )
            delegate.setExplorationProbability(
                    Math.max(delegate.getExplorationProbability() * (1d - incrementMultiplier), explorationMinimum));

        assert delegate.getExplorationProbability() >=explorationMinimum;
        assert delegate.getExplorationProbability() >=0;

        delegate.judgeExploration(previousFitness, currentFitness);
    }
}
