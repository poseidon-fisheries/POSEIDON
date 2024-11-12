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

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.behaviours.travel.TravelBehaviour;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.LocalDateTime;

@RequiredArgsConstructor
class ChooseDestinationBehaviour implements DestinationBehaviour {

    private final DestinationSupplier destinationSupplier;
    private final TravelBehaviour travelBehaviour;

    @Override
    public Action newAction(
        final LocalDateTime dateTime,
        final Vessel vessel
    ) {
        travelBehaviour.setCurrentDestination(destinationSupplier.get());
        return travelBehaviour.newAction(dateTime, vessel);
    }
}
