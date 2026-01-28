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

package uk.ac.ox.poseidon.agents.tasks.travel;

import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.tasks.AgentTask;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.PACKAGE;
import static tech.units.indriya.unit.Units.KILOMETRE_PER_HOUR;

@RequiredArgsConstructor(access = PACKAGE)
public class TravelAlongPath extends AgentTask<Vessel> {

    private final PathFinder<Int2D> pathFinder;
    private final DistanceCalculator distanceCalculator;
    private List<Int2D> currentPath;

    private Trip trip;
    private LocalDateTime startDateTime;
    private Int2D origin;
    private Int2D destination;
    private double cruisingSpeedInKph;

    @Override
    public void resetTask() {
        currentPath = null;
        super.resetTask();
    }

    @Override
    public void start() {
        final Vessel vessel = getAgent();
        trip = checkNotNull(vessel.getCurrentTrip());
        startDateTime = vessel.getSchedule().getDateTime();
        origin = vessel.getCell();
        destination = checkNotNull(vessel.getCurrentTrip().getDestination());
        currentPath =
            pathFinder
                .getPath(vessel.getCell(), destination)
                .orElseThrow(() -> new IllegalStateException(
                    MessageFormat.format(
                        "No path found from {0} to {1} for vessel {2}.",
                        vessel.getCell(),
                        destination,
                        vessel
                    )
                ));
        cruisingSpeedInKph =
            vessel.getEngine()
                .getCruisingSpeed()
                .to(KILOMETRE_PER_HOUR)
                .getValue()
                .doubleValue();
        super.start();
    }

    @Override
    public Status execute() {
        final Vessel vessel = getAgent();
        final Int2D destinationCell = checkNotNull(getAgent().getCurrentTrip().getDestination());
        checkState(
            // TODO: consider whether we should reroute instead
            currentPath.getLast().equals(destinationCell),
            "Current path %s does not match current destination %s for vessel %s.",
            currentPath,
            destinationCell,
            vessel
        );

        vessel.setCurrentCell(currentPath.getFirst());
        currentPath = currentPath.subList(1, currentPath.size());
        if (currentPath.isEmpty()) {
            trip.getEventManager().broadcast(new TravelEvent(
                getAgent(),
                startDateTime,
                getAgent().getSchedule().getDateTime(),
                origin,
                destination
            ));
            return SUCCEEDED;
        } else {
            final Int2D nextCell = currentPath.getFirst();
            vessel.setHeadingTowards(nextCell);
            vessel.setTaskDuration(
                distanceCalculator.travelDuration(
                    List.of(vessel.getCell(), nextCell),
                    cruisingSpeedInKph
                )
            );
            return RUNNING;
        }
    }

}
