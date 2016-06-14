package uk.ac.ox.oxfish.fisher.strategies.gear;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * The strategy the agent uses to decide which gear to use before starting the trip
 * Created by carrknight on 6/13/16.
 */
public interface GearStrategy extends FisherStartable
{


    /**
     * choose gear to use for this trip
     * @param fisher the agent making a choice
     * @param random the randomizer
     * @param model the model
     * @param currentAction the action that triggered a call to this strategy
     */
    public void updateGear(Fisher fisher,
                           MersenneTwisterFast random,
                           FishState model,
                           Action currentAction);






}
