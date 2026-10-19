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

package uk.ac.ox.poseidon.fishing.travel;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.BiFunction;
import sim.util.Int2D;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.PathLengthCalculator;

/**
 * The {@link Route} a vessel would travel to fish at a destination cell: from
 * its current cell to the destination, then back to its home port (from port,
 * the round trip). The vessel's cell, home port and cruising speed are read at
 * each call.
 */
public class RouteViaDestination implements BiFunction<Vessel, Int2D, Route> {

  private final VesselComponent<? extends Engine> engine;
  private final PathLengthCalculator pathLengthCalculator;

  /**
   * @param engine               the engine whose speed gives the route's
   *                             duration
   * @param pathLengthCalculator measures the route's two legs
   */
  RouteViaDestination(
    final VesselComponent<? extends Engine> engine,
    final PathLengthCalculator pathLengthCalculator
  ) {
    this.engine = checkNotNull(engine);
    this.pathLengthCalculator = checkNotNull(pathLengthCalculator);
  }

  /**
   * @param vessel      the vessel that would travel the route
   * @param destination the cell to fish at
   * @return the route from the vessel's current cell to {@code destination},
   *         then back to its home port
   * @throws IllegalArgumentException if either leg has no path
   */
  @Override
  public Route apply(final Vessel vessel, final Int2D destination) {
    final double distanceInKm =
      pathLengthCalculator.lengthInKm(vessel.getCell(), destination) +
      pathLengthCalculator.lengthInKm(destination, vessel.getHomePortCell());
    return new Route(
      distanceInKm,
      DistanceCalculator.travelDuration(
        distanceInKm,
        engine.of(vessel).getCruisingSpeedInKph()
      )
    );
  }
}
