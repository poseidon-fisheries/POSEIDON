package uk.ac.ox.oxfish.fisher.actions;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This is the starting state and is in general the state the fisher stays at while at port
 * Created by carrknight on 4/18/15.
 */
public class AtPort implements Action {

    /**
     * Asks the fisher if they want to move, otherwise stay at port.
     *
     * @param model a link to the model, in case you need to grab global objects
     * @param agent a link to the fisher in case you need to get or set agent's variables
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(FishState model, Fisher agent)
    {
        if(agent.shouldFisherLeavePort(model))
        {
            agent.updateDestination(model,this);
            assert !agent.getDestination().equals(agent.getHomePort().getLocation()); //shouldn't have chosen to go to port because that's weird
            return new ActionResult(new Move(),true);
        }
        else //if you don't want to leave port, stay home
            return new ActionResult(this,false);


    }
}
