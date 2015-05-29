package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The strategy that decides whether or not to fish once arrived and keeps being queried
 * Created by carrknight on 4/22/15.
 */
public interface FishingStrategy {

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     * @param fisher the agent
     * @param random the randomizer
     * @param model the model itself
     * @return true if the fisher should fish here, false otherwise
     */
    public boolean shouldFish(Fisher fisher, MersenneTwisterFast random, FishState model);

}
