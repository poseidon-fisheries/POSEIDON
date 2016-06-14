package uk.ac.ox.oxfish.fisher.strategies.gear;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Always returns the same gear every day
 * Created by carrknight on 6/13/16.
 */
public class FixedGearStrategy implements GearStrategy {
    /**
     * choose gear to use for this trip
     *
     * @param fisher        the agent making a choice
     * @param random        the randomizer
     * @param model         the model
     * @param currentAction the action that triggered a call to this strategy
     * @return the gear to use. Null can be returned to mean: "use current gear"
     */
    @Override
    public void updateGear(
            Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {
    }

    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff() {

    }
}
