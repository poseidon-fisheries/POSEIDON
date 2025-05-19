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

package uk.ac.ox.poseidon.agents.behaviours.strategy;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.behaviours.BranchingBehaviour;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.agents.behaviours.strategy.ThereAndBack.Status.*;

@RequiredArgsConstructor(access = PACKAGE)
public class ThereAndBack extends BranchingBehaviour {

    private final Behaviour fishingDestinationBehaviour;
    private final Behaviour fishingBehaviour;
    private final Behaviour travellingBehaviour;
    private Status status = READY;

    @Override
    protected Behaviour nextBehaviour(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        if (status == DONE) {
            status = READY;
            return null;
        } else if (status == READY) {
            status = ACTIVE;
            return fishingDestinationBehaviour;
        } else if (vessel.isAtCurrentDestination()) {
            status = DONE;
            return fishingBehaviour;
        } else {
            return travellingBehaviour;
        }
    }

    enum Status {
        READY, ACTIVE, DONE
    }

}
