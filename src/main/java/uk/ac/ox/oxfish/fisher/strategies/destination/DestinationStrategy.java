package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Decision-making of the fisher to choose where to go next!
 * Created by carrknight on 4/19/15.
 */
public interface DestinationStrategy {


    /**
     * decides where to go.
     * @param fisher the agent that needs to choose
     * @param random the randomizer. It probably comes from the fisher but I make explicit it might be needed
     *@param model the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    SeaTile chooseDestination(Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction);


}
