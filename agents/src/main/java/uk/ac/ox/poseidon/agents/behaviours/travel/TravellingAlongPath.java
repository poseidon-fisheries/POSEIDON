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
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.behaviours.SteppableAction;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.Distance;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@RequiredArgsConstructor
public class TravellingAlongPath implements Behaviour {

    private final PathFinder<Int2D> pathFinder;
    private final Distance distance;
    private List<Int2D> currentPath;

    @Override
    public SteppableAction nextAction(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        final Int2D currentDestination = checkNotNull(vessel.getCurrentDestination());
        if (currentPath != null) {
            checkState(
                currentPath.getLast().equals(currentDestination),
                "Current path %s does not match current destination %s for vessel %s.",
                currentPath,
                currentDestination,
                vessel
            );
        } else {
            currentPath =
                pathFinder
                    .getPath(vessel.getCurrentCell(), currentDestination)
                    .filter(path -> path.size() > 1)
                    .map(path -> path.subList(1, path.size()))
                    .orElseThrow(() -> new IllegalStateException(
                        MessageFormat.format(
                            "No path found from {0} to {1} for vessel {2}.",
                            vessel.getCurrentCell(),
                            currentDestination,
                            vessel
                        )
                    ));
        }
        return new Action(vessel, dateTime);
    }

    @ToString(callSuper = true)
    public class Action extends SteppableAction {

        private Action(
            final Vessel vessel,
            final LocalDateTime start
        ) {
            super(
                vessel,
                start,
                distance.travelDuration(
                    vessel.getCurrentCell(),
                    currentPath.getFirst(),
                    vessel.getCruisingSpeed()
                )
            );
        }

        @Override
        public void init() {
            // this is purely for visualisation purposes
            vessel.setHeadingTowards(currentPath.getFirst());
        }

        @Override
        public void complete(final LocalDateTime dateTime) {
            vessel.setCurrentCell(currentPath.getFirst());
            if (currentPath.size() > 1) {
                currentPath = currentPath.subList(1, currentPath.size());
            } else {
                currentPath = null;
                vessel.popBehaviour();
            }
        }
    }
}
