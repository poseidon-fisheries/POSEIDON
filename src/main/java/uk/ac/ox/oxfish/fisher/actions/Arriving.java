/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * Arrived to destination, should I fish or look for another destination?
 * Created by carrknight on 4/19/15.
 */
public class Arriving implements Action{

    /**
     * Do something and returns a result which is the next state and whether or not it should be lspiRun on the same turn
     *
     * @param model a link to the model, in case you need to grab global objects
     * @param agent a link to the fisher in case you need to get or set agent's variables
     * @param regulation regulation that tells us whether we can fish here or not
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(FishState model, Fisher agent, Regulation regulation,double hoursLeft) {
        assert agent.isAtDestination();

        //did we arrive at port? then dock
        if (agent.getLocation().equals(agent.getHomePort().getLocation()))
            return new ActionResult(new Docking(),hoursLeft);

        //adapt if needed
        agent.updateDestination(model,this);
        //we don't want to move anywhere else
        if(agent.getDestination().equals(agent.getLocation())) {
            return agent.getFishingStrategy().act(model, agent, regulation, hoursLeft);
        }
        //we got a new location? move to there!
        else
        {
            return new ActionResult(new Moving(), hoursLeft);
        }
    }
}
