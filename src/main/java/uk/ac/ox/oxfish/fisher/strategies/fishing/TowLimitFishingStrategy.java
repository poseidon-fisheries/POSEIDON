package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This should be turned into a regulation at some point, but this represents
 * a fisher who stops towing after doing it x times
 * Created by carrknight on 6/21/17.
 */
public class TowLimitFishingStrategy implements FishingStrategy {

    private final int maxNumberOfTows;

    public TowLimitFishingStrategy(int maxNumberOfTows) {
        this.maxNumberOfTows = maxNumberOfTows;
    }

    private final FishUntilFullStrategy delegate = new FishUntilFullStrategy(1.0);


    @Override
    public void start(FishState model, Fisher fisher)
    {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     * @param fisher
     * @param random      the randomizer
     * @param model       the model itself
     * @param currentTrip
     * @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
            Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return delegate.shouldFish(fisher, random, model, currentTrip) &&
                currentTrip.getEffort()<=maxNumberOfTows;
    }

    /**
     * Getter for property 'maxNumberOfTows'.
     *
     * @return Value for property 'maxNumberOfTows'.
     */
    public int getMaxNumberOfTows() {
        return maxNumberOfTows;
    }


}

