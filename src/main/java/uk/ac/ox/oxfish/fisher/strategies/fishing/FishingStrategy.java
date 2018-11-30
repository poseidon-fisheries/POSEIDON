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

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.actions.Fishing;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * The strategy that decides whether or not to fish once arrived and keeps being queried
 * Created by carrknight on 4/22/15.
 */
public interface FishingStrategy extends FisherStartable, Action {

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     * @param random the randomizer
     * @param model the model itself
     * @return true if the fisher should fish here, false otherwise
     */
    boolean shouldFish(
            Fisher fisher,
            MersenneTwisterFast random,
            FishState model,
            TripRecord currentTrip);

    /**
     * This is called by Arriving.act to decide whether or not to fish up arrival. Most fishing
     * strategies should use this default implementation, but FAD fishing strategies are expected to
     * override this method and result in action types other than `Fishing`.
     */
    @Override
    default ActionResult act(
        FishState model, Fisher agent, Regulation regulation, double hoursLeft
    ) {
        return agent.canAndWantToFishHere() ?
            new ActionResult(new Fishing(), hoursLeft) : // if we want to fish here, let's fish
            new ActionResult(new Arriving(), 0d); // otherwise, basically wait
    }

}
