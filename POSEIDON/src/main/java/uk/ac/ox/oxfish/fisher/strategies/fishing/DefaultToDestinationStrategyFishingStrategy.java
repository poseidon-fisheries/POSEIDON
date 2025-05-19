/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * this class assumes the fisher's destination strategy is actually also a fishing strategy and will just use it
 */
public class DefaultToDestinationStrategyFishingStrategy implements FishingStrategy {

    @Override
    public boolean shouldFish(Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return ((FishingStrategy) fisher.getDestinationStrategy()).shouldFish(fisher, random, model, currentTrip);
    }

    @Override
    public ActionResult act(FishState model, Fisher agent, Regulation regulation, double hoursLeft) {
        return ((FishingStrategy) agent.getDestinationStrategy()).act(model, agent, regulation, hoursLeft);
    }

    //I assume the destination strategy takes care of itself
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }
}
