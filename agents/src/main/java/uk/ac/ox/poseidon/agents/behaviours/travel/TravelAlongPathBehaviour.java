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
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.Distance;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

class TravelAlongPathBehaviour extends AbstractTravelBehaviour {

    private final PathFinder<Int2D> pathFinder;
    private List<Int2D> currentPath;

    TravelAlongPathBehaviour(
        final Behaviour behaviourOnArrival,
        final PathFinder<Int2D> pathFinder,
        final Distance distance
    ) {
        super(distance, behaviourOnArrival);
        this.pathFinder = pathFinder;
    }

    @Override
    public Action newAction(
        final LocalDateTime dateTime,
        final Vessel vessel
    ) {
        final Int2D vesselLocation =
            vessel.getVesselField().getGridExtent().toCell(vessel.getCurrentPoint());
        currentPath =
            pathFinder
                .getPath(vesselLocation, currentDestination)
                .orElseThrow(
                    () -> new IllegalStateException(
                        MessageFormat.format(
                            "No path found from {0} to {1} for vessel {2}",
                            vesselLocation,
                            currentDestination,
                            vessel
                        )
                    )
                );
        return nextAction(dateTime, vessel);
    }

    private Action nextAction(
        final LocalDateTime dateTime,
        final Vessel vessel
    ) {
        if (currentPath.size() > 1) {
            currentPath = currentPath.subList(1, currentPath.size());
            vessel.setHeadingTowards(currentPath.getFirst());
            return new TravellingAlongPath(dateTime, vessel);
        } else {
            currentPath = null;
            return behaviourOnArrival.newAction(dateTime, vessel);
        }
    }

    @ToString(callSuper = true)
    public class TravellingAlongPath extends Action {

        private TravellingAlongPath(
            final LocalDateTime start,
            final Vessel vessel
        ) {
            super(start, travelDuration(vessel, currentPath.getFirst()), vessel);
        }

        @Override
        protected Action complete(final LocalDateTime dateTime) {
            getVessel().setLocation(currentPath.getFirst());
            return nextAction(dateTime, getVessel());
        }
    }
}
