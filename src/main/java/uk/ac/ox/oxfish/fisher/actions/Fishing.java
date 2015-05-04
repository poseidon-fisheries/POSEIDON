package uk.ac.ox.oxfish.fisher.actions;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulations;

/**
 * For now fish means use gear here (checking that we are at destination and it makes sense)
 * Created by carrknight on 4/22/15.
 */
public class Fishing implements Action
{

    /**
     * If you are at destination, and the destination is a sea tile then use the gear to fish
     *
     * @param model a link to the model, in case you need to grab global objects
     * @param agent a link to the fisher in case you need to get or set agent's variables
     * @param regulations regulations that tells us if we are allowed to fish
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(FishState model, Fisher agent, Regulations regulations) {

        Preconditions.checkArgument(agent.isAtDestination()); //you arrived
        Preconditions.checkArgument(agent.getLocation().getAltitude() <= 0); //you are at sea
        Preconditions.checkState(regulations.canFishHere(agent,agent.getLocation(), model)); //i should be allowed to fish here!
        //fish!
        agent.fishHere(model.getBiology());

        //go back to "arrived" state
        return new ActionResult(new Arriving(),false);

    }
}
