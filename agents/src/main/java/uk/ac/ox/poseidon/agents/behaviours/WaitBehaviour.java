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

package uk.ac.ox.poseidon.agents.behaviours;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Getter
@AllArgsConstructor
public class WaitBehaviour implements Behaviour {

    private final Supplier<Duration> waitingDurationSupplier;
    private final Behaviour afterWaitingBehaviour;

    @Override
    public Action newAction(
        final LocalDateTime dateTime,
        final Vessel vessel
    ) {
        return new Waiting(dateTime, waitingDurationSupplier.get(), vessel);
    }

    @ToString(callSuper = true)
    public class Waiting extends Action {

        public Waiting(
            final LocalDateTime start,
            final Duration duration,
            final Vessel vessel
        ) {
            super(start, duration, vessel);
        }

        @Override
        protected Action complete(
            final LocalDateTime dateTime
        ) {
            return afterWaitingBehaviour.newAction(dateTime, getVessel());
        }
    }

}
