/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.geography.distance.DistanceCalculator.travelDuration;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.tasks.VesselTask;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponent;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.tanks.FuelTank;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

/**
 * A leaf {@link VesselTask} that moves the vessel cell by cell along a path from
 * its current cell to a destination, resolved once at {@link #start()},
 * consuming fuel and taking the travel duration for each hop; broadcasts a
 * {@link TravelEvent} on arrival.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class TravelAlongPath extends VesselTask {

  private final VesselComponent<? extends Engine> engine;
  private final VesselComponent<? extends FuelTank> fuelTank;
  private final PathFinder<Int2D> pathFinder;
  private final DistanceCalculator distanceCalculator;
  private final Function<? super Vessel, Int2D> destinationSource;
  private final Function<? super Vessel, EventManager> eventManager;
  private final Function<? super Vessel, LocalDateTime> currentDateTime;
  private List<Int2D> currentPath;

  private LocalDateTime startDateTime;
  private Int2D origin;
  private Int2D destination;
  private double cruisingSpeedInKph;
  private double distanceToNextCell;

  /**
   * Clears the remaining path, so the next run resolves it afresh.
   */
  @Override
  public void resetTask() {
    currentPath = null;
    super.resetTask();
  }

  /**
   * Resolves the destination and a path to it from the vessel's current cell.
   *
   * @throws IllegalStateException if no path to the destination exists
   */
  @Override
  public void start() {
    final Vessel vessel = getVessel();
    destination = checkNotNull(destinationSource.apply(vessel));
    startDateTime = currentDateTime.apply(vessel);
    origin = vessel.getCell();
    currentPath = pathFinder
      .getPath(vessel.getCell(), destination)
      .orElseThrow(() ->
        new IllegalStateException(
          MessageFormat.format(
            "No path found from {0} to {1} for vessel {2}.",
            vessel.getCell(),
            destination,
            vessel
          )
        )
      );
    distanceToNextCell = 0;
    cruisingSpeedInKph = engine.of(vessel).getCruisingSpeedInKph();
    super.start();
  }

  /**
   * Moves the vessel to the next cell on the path, taking the fuel the engine
   * burned over the hop just completed from the tank. Broadcasts a {@link TravelEvent} and succeeds once the
   * destination is reached; otherwise computes the next hop's distance/duration
   * and reports {@code RUNNING}.
   */
  @Override
  public Status execute() {
    final Vessel vessel = getVessel();
    checkState(
      currentPath.getLast().equals(destination),
      "Current path %s does not match current destination %s for vessel %s.",
      currentPath,
      destination,
      vessel
    );

    vessel.setCurrentCell(currentPath.getFirst());
    fuelTank
      .of(vessel)
      .consumeFuel(
        engine.of(vessel).getLitresOfFuelConsumedPerKm() * distanceToNextCell
      );

    currentPath = currentPath.subList(1, currentPath.size());
    if (currentPath.isEmpty()) {
      eventManager
        .apply(vessel)
        .broadcast(
          new TravelEvent(
            getVessel(),
            startDateTime,
            currentDateTime.apply(vessel),
            origin,
            destination
          )
        );
      return SUCCEEDED;
    } else {
      final Int2D nextCell = currentPath.getFirst();
      distanceToNextCell = distanceCalculator.distanceInKm(
        vessel.getCell(),
        nextCell
      );
      vessel.setHeadingTowards(nextCell);
      return runFor(travelDuration(distanceToNextCell, cruisingSpeedInKph));
    }
  }
}
