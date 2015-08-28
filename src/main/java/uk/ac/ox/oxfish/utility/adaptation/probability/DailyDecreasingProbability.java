package uk.ac.ox.oxfish.utility.adaptation.probability;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * Probability of exploring decreases by fixed value every day, up to a threshold. Imitation is untouched
 * Created by carrknight on 8/28/15.
 */
public class DailyDecreasingProbability implements AdaptationProbability, Steppable {



    private final  double dailyDecreaseMultiplier;


    private final FixedProbability probability;

    private final double explorationThreshold;


    public DailyDecreasingProbability(
            double explorationProbability, double imitationProbability,
            double dailyDecreaseMultiplier, double explorationThreshold) {
        this.probability = new FixedProbability(explorationProbability, imitationProbability);
        this.dailyDecreaseMultiplier = dailyDecreaseMultiplier;
        this.explorationThreshold = explorationThreshold;
    }

    @Override
    public double getExplorationProbability() {
        return probability.getExplorationProbability();
    }

    @Override
    public double getImitationProbability() {
        return probability.getImitationProbability();
    }

    @Override
    public void judgeExploration(double previousFitness, double currentFitness) {
        probability.judgeExploration(previousFitness,currentFitness);
    }

    /**
     * schedule yourself to lower exploration rates after a bit.
     *
     * @param model the model
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        model.scheduleEveryDay(
                this,
                StepOrder.DAWN);

    }


    @Override
    public void step(SimState simState) {
        probability.
                setExplorationProbability(
                        Math.max(probability.getExplorationProbability() * dailyDecreaseMultiplier,
                                 explorationThreshold));
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }

    public double getDailyDecreaseMultiplier() {
        return dailyDecreaseMultiplier;
    }

    public void setExplorationProbability(double explorationProbability) {
        probability.setExplorationProbability(explorationProbability);
    }

    public void setImitationProbability(double imitationProbability) {
        probability.setImitationProbability(imitationProbability);
    }

    public double getExplorationThreshold() {
        return explorationThreshold;
    }
}
