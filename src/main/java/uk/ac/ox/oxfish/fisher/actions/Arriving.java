package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulations;

/**
 * Arrived to destination, should I fish or look for another destination?
 * Created by carrknight on 4/19/15.
 */
public class Arriving implements Action{

    /**
     * Do something and returns a result which is the next state and whether or not it should be run on the same turn
     *
     * @param model a link to the model, in case you need to grab global objects
     * @param agent a link to the fisher in case you need to get or set agent's variables
     * @param regulations regulations that tells us whether we can fish here or not
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(FishState model, Fisher agent, Regulations regulations) {
        assert agent.isAtDestination();

        if (agent.getLocation().equals(agent.getHomePort().getLocation()))
            return new ActionResult(new Docking(),true);

        if(regulations.canFishHere(agent,agent.getLocation(), model)
                &&
                agent.shouldIFish(model)) //if you want to fish
            return new ActionResult(new Fishing(),true);



        //adapt if needed
        agent.updateDestination(model,this);
        if(agent.getDestination().equals(agent.getLocation()))
            return new ActionResult(new Arriving(), false);
        else
            return new ActionResult(new Moving(),true);
    }
}
