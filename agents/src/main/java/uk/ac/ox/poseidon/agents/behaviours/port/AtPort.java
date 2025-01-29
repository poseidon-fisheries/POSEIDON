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

package uk.ac.ox.poseidon.agents.behaviours.port;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.behaviours.SteppableAction;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.LocalDateTime;
import java.util.function.BooleanSupplier;

import static com.google.common.base.Preconditions.checkState;

@RequiredArgsConstructor
public class AtPort implements Behaviour {

    // TODO: handle hold emptying

    private final BooleanSupplier isReady;
    private final Behaviour dockingBehaviour;
    private final Behaviour behaviourIfReady;
    private final Behaviour behaviourIfNotReady;

    @Override
    public SteppableAction nextAction(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        checkState(vessel.isAtPort());
        final Behaviour nextBehaviour;
        if (vessel.getCurrentDestination() != null)
            nextBehaviour = dockingBehaviour;
        else if (isReady.getAsBoolean())
            nextBehaviour = behaviourIfReady;
        else
            nextBehaviour = behaviourIfNotReady;
        vessel.pushBehaviour(nextBehaviour);
        return nextBehaviour.nextAction(vessel, dateTime);
    }
}
