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

import lombok.ToString;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.Distance;

import java.time.Duration;
import java.time.LocalDateTime;

class TravelDirectlyBehaviour extends AbstractTravelBehaviour {

    TravelDirectlyBehaviour(
        final Distance distance,
        final Behaviour behaviourOnArrival
    ) {
        super(distance, behaviourOnArrival);
    }

    @Override
    public Action newAction(
        final LocalDateTime dateTime,
        final Vessel vessel
    ) {
        return new TravellingDirectly(
            dateTime,
            travelDuration(vessel, currentDestination),
            vessel
        );
    }

    @ToString(callSuper = true)
    public class TravellingDirectly extends Action {

        private TravellingDirectly(
            final LocalDateTime start,
            final Duration duration,
            final Vessel agent
        ) {
            super(start, duration, agent);
        }

        @Override
        protected Action complete(final LocalDateTime dateTime) {
            getVessel().setLocation(currentDestination);
            currentDestination = null;
            return behaviourOnArrival.newAction(dateTime, getVessel());
        }
    }
}
