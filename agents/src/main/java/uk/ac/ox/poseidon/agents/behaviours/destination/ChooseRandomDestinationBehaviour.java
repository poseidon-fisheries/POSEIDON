/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.behaviours.destination;

import ec.util.MersenneTwisterFast;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.travel.TravelBehaviour;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.List;

import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;

public class ChooseRandomDestinationBehaviour extends DestinationBehaviour {

    private final List<Int2D> possibleDestinations;
    private final MersenneTwisterFast rng;

    public ChooseRandomDestinationBehaviour(
        final TravelBehaviour travelBehaviour,
        final List<Int2D> possibleDestinations,
        final MersenneTwisterFast rng
    ) {
        super(travelBehaviour);
        this.possibleDestinations = possibleDestinations;
        this.rng = rng;
    }

    @Override
    protected Int2D chooseDestination(final Vessel vessel) {
        return oneOf(possibleDestinations, rng);
    }
}
