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

package uk.ac.ox.poseidon.agents.behaviours.travel;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.behaviours.SteppableAction;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

import java.time.Duration;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class TravellingDirectly implements Behaviour {

    private final DistanceCalculator distanceCalculator;

    @Override
    public SteppableAction nextAction(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        return new Action(
            dateTime,
            distanceCalculator.travelDuration(
                vessel.getCell(),
                vessel.getCurrentDestination(),
                vessel.getCruisingSpeed()
            ),
            vessel
        );
    }

    @ToString(callSuper = true)
    public static class Action extends SteppableAction {

        private Action(
            final LocalDateTime start,
            final Duration duration,
            final Vessel vessel
        ) {
            super(vessel, start, duration);
        }

        @Override
        public void complete(final LocalDateTime dateTime) {
            getVessel().setCurrentCell(getVessel().getCurrentDestination());
            getVessel().popBehaviour();
        }
    }
}
