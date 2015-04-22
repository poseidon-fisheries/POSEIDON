package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Arrived to destination, should I fish or look for another destination?
 * Created by carrknight on 4/19/15.
 */
public class Arrived implements Action{

    /**
     * Do something and returns a result which is the next state and whether or not it should be run on the same turn
     *
     * @param model a link to the model, in case you need to grab global objects
     * @param agent a link to the fisher in case you need to get or set agent's variables
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(FishState model, Fisher agent) {
        assert agent.isAtDestination();

        if(agent.shouldIFish(model)) //if you want to fish
                return new ActionResult(new FishHere(),true);


        //adapt if needed
        agent.updateDestination(model,this);
        if(agent.getDestination().equals(agent.getLocation()))
            return new ActionResult(new Arrived(),false);
        else
            return new ActionResult(new Move(),true);
    }
}
