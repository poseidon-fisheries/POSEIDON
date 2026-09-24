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
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.geography.distance.DistanceCalculator.travelDuration;

/**
 * A leaf {@link AgentTask} that moves the vessel cell by cell along a path from its current cell
 * to a destination, resolved once at {@link #start()}, consuming fuel and taking the travel
 * duration for each hop; broadcasts a {@link TravelEvent} on arrival.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class TravelAlongPath extends AgentTask<Vessel> {

    private final PathFinder<Int2D> pathFinder;
    private final DistanceCalculator distanceCalculator;
    private final Supplier<Int2D> destinationCell;
    private final Supplier<EventManager> eventManagerSupplier;
    private List<Int2D> currentPath;
    private EventManager eventManager;

    private LocalDateTime startDateTime;
    private Int2D origin;
    private Int2D destination;
    private double cruisingSpeedInKph;
    private double distanceToNextCell;

    /** Clears the remaining path and event manager, so the next run resolves them afresh. */
    @Override
    public void resetTask() {
        currentPath = null;
        eventManager = null;
        super.resetTask();
    }

    /**
     * Resolves the destination and a path to it from the vessel's current cell.
     *
     * @throws IllegalStateException if no path to the destination exists
     */
    @Override
    public void start() {
        final Vessel vessel = getAgent();
        destination = checkNotNull(destinationCell.get());
        eventManager = eventManagerSupplier.get();
        startDateTime = vessel.getSchedule().getDateTime();
        origin = vessel.getCell();
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
        distanceToNextCell = 0;
        cruisingSpeedInKph = vessel.getEngine().getCruisingSpeedInKph();
        super.start();
    }

    /**
     * Moves the vessel to the next cell on the path, consuming the fuel for the hop just
     * completed. Broadcasts a {@link TravelEvent} and succeeds once the destination is reached;
     * otherwise computes the next hop's distance/duration and reports {@code RUNNING}.
     */
    @Override
    public Status execute() {
        final Vessel vessel = getAgent();
        checkState(
            currentPath.getLast().equals(destination),
            "Current path %s does not match current destination %s for vessel %s.",
            currentPath,
            destination,
            vessel
        );

        vessel.setCurrentCell(currentPath.getFirst());
        vessel.getEngine().consumeFuelForDistance(distanceToNextCell);

        currentPath = currentPath.subList(1, currentPath.size());
        if (currentPath.isEmpty()) {
            eventManager.broadcast(new TravelEvent(
                getAgent(),
                startDateTime,
                getAgent().getSchedule().getDateTime(),
                origin,
                destination
            ));
            return SUCCEEDED;
        } else {
            final Int2D nextCell = currentPath.getFirst();
            distanceToNextCell = distanceCalculator.distanceInKm(vessel.getCell(), nextCell);
            vessel.setHeadingTowards(nextCell);
            vessel.setTaskDuration(travelDuration(distanceToNextCell, cruisingSpeedInKph));
            return RUNNING;
        }
    }
}
