package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The fisher keeps fishing until the percentage of hold filled is above a threshold.
 *
 * Created by carrknight on 5/5/15.
 */
public class FishUntilFullStrategy implements FishingStrategy {

    private double minimumPercentageFull;

    private final static double EPSILON = .001;

    public FishUntilFullStrategy(double minimumPercentageFull) {
        this.minimumPercentageFull = minimumPercentageFull;
    }

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     * @param fisher the agent
     * @param random the randomizer
     * @param model  the model itself
     * @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
            Fisher fisher, MersenneTwisterFast random, FishState model) {
        return fisher.getPoundsCarried() + EPSILON < fisher.getMaximumLoad() * minimumPercentageFull  ;
    }

    public double getMinimumPercentageFull() {
        return minimumPercentageFull;
    }

    public void setMinimumPercentageFull(double minimumPercentageFull) {
        this.minimumPercentageFull = minimumPercentageFull;
    }


    @Override
    public void start(FishState model, Fisher fisher) {
        //ignored
    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }
}



