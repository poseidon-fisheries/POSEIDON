/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * make the fisher waste some hours and then go back to Arriving action
 */
public class Delaying implements Action {

    private double hoursToWaste;

    public Delaying(double hoursToWaste) {
        Preconditions.checkArgument(hoursToWaste>0);
        this.hoursToWaste = hoursToWaste;
    }

    /**
     * Spend as many hours it needs to go to arriving or punts to next step
     *
     * @param model      a link to the model, in case you need to grab global objects
     * @param agent      a link to the fisher in case you need to get or set agent's variables
     * @param regulation the regulation governing the agent
     * @param hoursLeft  how much time is left (in hours) to act in this step
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(
            FishState model, Fisher agent, Regulation regulation, double hoursLeft) {
        if(hoursLeft >= hoursToWaste)
            return new ActionResult(new Arriving(),hoursLeft-hoursToWaste);
        else
            return new ActionResult(new Delaying(hoursToWaste - hoursLeft),0);
    }
}
