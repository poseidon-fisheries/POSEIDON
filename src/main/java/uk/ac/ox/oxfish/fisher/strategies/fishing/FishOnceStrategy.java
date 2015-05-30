package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishOnceFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;

/**
 * Fish only if the hold is empty, then just go back home
 * Created by carrknight on 5/5/15.
 */
public class FishOnceStrategy implements FishingStrategy {
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
        return fisher.getPoundsCarried() == 0;
    }


    /***
     *      ___ _   ___ _____ ___  _____   __
     *     | __/_\ / __|_   _/ _ \| _ \ \ / /
     *     | _/ _ \ (__  | || (_) |   /\ V /
     *     |_/_/ \_\___| |_| \___/|_|_\ |_|
     *
     */

    /**
     * returns a new instance of the FishOnceStrategy. It has no fields
     */
    public static  final StrategyFactory<FishOnceStrategy> FISH_ONCE_FACTORY = new FishOnceFactory();
    {

    };
}


