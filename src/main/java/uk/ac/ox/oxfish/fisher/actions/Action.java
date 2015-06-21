package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * This is the "state" of the fisher state machine.
 * Created by carrknight on 4/12/15.
 */
public interface Action {


    /**
     * Do something and returns a result which is the next state and whether or not it should be run on the same turn
     * @param model a link to the model, in case you need to grab global objects
     * @param agent a link to the fisher in case you need to get or set agent's variables
     * @param regulation the regulation governing the agent
     * @param hoursLeft how much time is left (in hours) to act in this step
     * @return the next action to take and whether or not to take it now
     */
    public ActionResult act(FishState model, Fisher agent, Regulation regulation, double hoursLeft);

}


