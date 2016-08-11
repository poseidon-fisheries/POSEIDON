package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
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
     *
     * @param equipment
     * @param status
     *@param memory
     * @param random the randomizer
     * @param model  the model itself   @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
            FisherEquipment equipment, FisherStatus status, FisherMemory memory, MersenneTwisterFast random,
            FishState model) {
        return equipment.getTotalPoundsCarried() + EPSILON < equipment.getMaximumLoad() * minimumPercentageFull  ;
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
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {

    }
}



