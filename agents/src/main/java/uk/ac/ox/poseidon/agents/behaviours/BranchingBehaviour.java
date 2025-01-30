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

package uk.ac.ox.poseidon.agents.behaviours;

import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.LocalDateTime;
import java.util.Optional;

public abstract class BranchingBehaviour implements Behaviour {
    @Override
    public final SteppableAction nextAction(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        return Optional
            .ofNullable(nextBehaviour(vessel, dateTime))
            .map(behaviour -> {
                vessel.pushBehaviour(behaviour);
                return behaviour.nextAction(vessel, dateTime);
            })
            .or(() -> {
                vessel.popBehaviour();
                return Optional
                    .ofNullable(vessel.currentBehaviour())
                    .map(behaviour -> behaviour.nextAction(vessel, dateTime));
            })
            .orElse(null);
    }

    protected abstract Behaviour nextBehaviour(
        final Vessel vessel,
        final LocalDateTime dateTime
    );
}
