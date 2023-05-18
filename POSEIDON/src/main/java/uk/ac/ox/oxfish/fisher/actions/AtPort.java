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
 * This is the starting state and is in general the state the fisher stays at while at port
 * Created by carrknight on 4/18/15.
 */
public class AtPort implements Action {

    /**
     * Asks the fisher if they want to move, otherwise stay at port.
     *
     * @param model      a link to the model, in case you need to grab global objects
     * @param agent      a link to the fisher in case you need to get or set agent's variables
     * @param regulation the regulation that tells us whether we can leave
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(FishState model, Fisher agent, Regulation regulation, double hoursLeft) {

        if ((regulation.allowedAtSea(agent, model) || agent.isCheater())
            &&
            agent.shouldFisherLeavePort(model)) {

            //departing!
            agent.updateGear(model.getRandom(), model, this);
            agent.updateDestination(model, this);
            //you can be redirected by the destination strategy to stay at port, check now
            if (agent.getDestination().equals(agent.getHomePort().getLocation()))
                return new ActionResult(this, Math.max(0, hoursLeft - 1));
            assert !agent.getDestination()
                .equals(agent.getHomePort().getLocation()); //shouldn't have chosen to go to port because that's weird
            agent.undock();
            return new ActionResult(new Moving(), hoursLeft);
        } else //you don't want to leave this hour, try again next hour
            return new ActionResult(this, Math.max(0, hoursLeft - 1));


    }
}
