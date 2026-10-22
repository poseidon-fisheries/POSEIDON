/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.fishing.fuel.FuelStationGrid;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.tanks.FuelTank;
import uk.ac.ox.poseidon.geography.Destination;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

/**
 * Factories for a vessel's travel-related behavior-tree tasks: refuel, travel,
 * land, end trip.
 */
public final class Factories {

  private Factories() {}

  /**
   * @param fuelTank        the tank to fill
   * @param fuelStationGrid the fuel stations a cell might contain
   * @return a {@link uk.ac.ox.poseidon.fishing.tasks.TaskFactory} for a {@link Refuel}
   * @see Refuel
   */
  public static RefuelFactory refuel(
    final VesselComponent<? extends FuelTank> fuelTank,
    final Factory<
      ? super VesselComponentScope,
      ? extends FuelStationGrid
    > fuelStationGrid
  ) {
    return new RefuelFactory(fuelTank, fuelStationGrid);
  }

  /**
   * @param engine       the engine that sets the speed and the fuel burned
   * @param fuelTank     the tank the fuel is taken from
   * @param pathFinder   finds a path between two cells
   * @param distance     computes the distance, and so the duration, of each
   *                     hop
   * @param destination  the destination to path to the cell of, read for the
   *                     vessel once, at task start
   * @param eventManager the event manager to broadcast the arrival
   *                     {@link TravelEvent} on, read for the vessel on arrival
   * @return a {@link uk.ac.ox.poseidon.fishing.tasks.TaskFactory} for a
   *         {@link TravelAlongPath}
   * @see TravelAlongPath
   */
  public static TravelAlongPathFactory travelAlongPathTo(
    final VesselComponent<? extends Engine> engine,
    final VesselComponent<? extends FuelTank> fuelTank,
    final Factory<
      ? super VesselComponentScope,
      ? extends PathFinder<Cell>
    > pathFinder,
    final Factory<
      ? super VesselComponentScope,
      ? extends DistanceCalculator
    > distance,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Destination
    > destination,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      EventManager
    > eventManager
  ) {
    return new TravelAlongPathFactory(
      engine,
      fuelTank,
      pathFinder,
      distance,
      destination,
      eventManager,
      currentDateTime()
    );
  }

  /**
   * @param engine   the engine that sets the speed and the fuel burned
   * @param fuelTank the tank the fuel is taken from
   * @param distance computes the direct distance, and so the duration, to the
   *                 trip's destination
   * @return a {@link uk.ac.ox.poseidon.fishing.tasks.TaskFactory} for a {@link TravelDirectly}
   * @see TravelDirectly
   */
  public static TravelDirectlyFactory travelDirectly(
    final VesselComponent<? extends Engine> engine,
    final VesselComponent<? extends FuelTank> fuelTank,
    final Factory<
      ? super VesselComponentScope,
      ? extends DistanceCalculator
    > distance
  ) {
    return new TravelDirectlyFactory(
      engine,
      fuelTank,
      distance,
      currentDateTime()
    );
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.fishing.tasks.TaskFactory} for a
   *         {@link SetDestinationToOrigin}
   * @see SetDestinationToOrigin
   */
  public static SetDestinationToOriginFactory setDestinationToOrigin() {
    return new SetDestinationToOriginFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.fishing.tasks.TaskFactory} for a
   *         {@link DockAtDestination}
   * @see DockAtDestination
   */
  public static DockAtDestinationFactory dockAtDestination() {
    return new DockAtDestinationFactory(currentDateTime());
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.fishing.tasks.TaskFactory} for an
   *         {@link Undock}, ending a stop mid-trip
   * @see Undock
   */
  public static UndockFactory undock() {
    return new UndockFactory(currentDateTime());
  }
}
