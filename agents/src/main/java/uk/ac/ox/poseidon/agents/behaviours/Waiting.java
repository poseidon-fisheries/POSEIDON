/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.behaviours;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Waiting implements Behaviour {

    private final Supplier<Duration> durationSupplier;

    @Override
    public SteppableAction nextAction(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        return new Action(vessel, dateTime, durationSupplier.get());
    }

    @ToString(callSuper = true)
    private static class Action extends SteppableAction {

        private Action(
            final Vessel vessel,
            final LocalDateTime start,
            final Duration duration
        ) {
            super(vessel, start, duration, vessel.getCoordinate());
        }

        @Override
        public void complete(final LocalDateTime dateTime) {
            getVessel().popBehaviour();
        }
    }
}
