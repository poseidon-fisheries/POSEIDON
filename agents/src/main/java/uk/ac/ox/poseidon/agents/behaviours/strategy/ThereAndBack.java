/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.agents.behaviours.strategy;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.behaviours.BranchingBehaviour;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ThereAndBack extends BranchingBehaviour {

    private final Behaviour fishingDestinationBehaviour;
    private final Behaviour fishingBehaviour;
    private final Behaviour travellingBehaviour;

    private boolean done = false;

    @Override
    protected Behaviour nextBehaviour(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        if (done) {
            return null;
        } else if (vessel.isAtPort()) {
            return fishingDestinationBehaviour;
        } else if (vessel.isAtCurrentDestination()) {
            done = true;
            return fishingBehaviour;
        } else {
            return travellingBehaviour;
        }
    }

}
