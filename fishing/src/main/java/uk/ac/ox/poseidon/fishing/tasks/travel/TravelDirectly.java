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

package uk.ac.ox.poseidon.fishing.tasks.travel;

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.geography.distance.DistanceCalculator.travelDuration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.tasks.ExtendedTripTask;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.tanks.FuelTank;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * An {@link ExtendedTripTask} that travels straight (as the crow flies) from
 * the vessel's current cell to its trip's destination, in one step, taking the
 * direct distance's travel duration; broadcasts a {@link TravelEvent} on
 * arrival.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class TravelDirectly extends ExtendedTripTask {

  private final VesselComponent<? extends Engine> engine;
  private final VesselComponent<? extends FuelTank> fuelTank;
  private final DistanceCalculator distanceCalculator;
  private final Function<? super Vessel, LocalDateTime> currentDateTime;
  private LocalDateTime startDateTime;
  private Cell origin;
  private Cell destination;
  private double distanceInKm;

  /**
   * Resolves the start date, the origin, the cell of the trip's destination,
   * and the direct distance between them.
   */
  @Override
  public void start() {
    super.start();
    startDateTime = currentDateTime.apply(getVessel());
    origin = getVessel().getCell();
    destination = getVessel()
      .getCurrentTrip()
      .getDestination()
      .getCell(getVessel().getPortGrid());
    distanceInKm = distanceCalculator.distanceInKm(origin, destination);
  }

  /**
   * @return the travel duration for {@link #distanceInKm} at the vessel's
   *         cruising speed
   */
  @Override
  protected Duration getDuration() {
    return travelDuration(
      distanceInKm,
      engine.of(getVessel()).getCruisingSpeedInKph()
    );
  }

  /**
   * Takes the fuel the engine burns over {@link #distanceInKm} from the tank,
   * moves the vessel to the
   * destination, and broadcasts a {@link TravelEvent}.
   *
   * @return always {@code SUCCEEDED}
   */
  @Override
  protected Status complete() {
    fuelTank
      .of(getVessel())
      .consumeFuel(
        engine.of(getVessel()).getLitresOfFuelConsumedPerKm() * distanceInKm
      );
    getVessel().setCurrentCell(destination);
    getTrip()
      .getEventManager()
      .broadcast(
        new TravelEvent(
          getVessel(),
          startDateTime,
          currentDateTime.apply(getVessel()),
          origin,
          destination
        )
      );
    return SUCCEEDED;
  }
}
