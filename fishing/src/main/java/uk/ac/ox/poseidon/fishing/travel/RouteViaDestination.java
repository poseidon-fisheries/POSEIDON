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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import java.util.function.BiFunction;
import sim.util.Int2D;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

/**
 * The {@link Route} a vessel would sail to fish at a destination cell: from its
 * current cell to the destination, then back to its home port (from port, the
 * round trip). The vessel's cell, home port and cruising speed are read at each
 * call. The length of the path between two cells is remembered after it is
 * first summed: it only depends on the two cells, and the path finder gives the
 * same path for them throughout a run.
 */
public class RouteViaDestination implements BiFunction<Vessel, Int2D, Route> {

  private final VesselComponent<? extends Engine> engine;
  private final GridPathFinder pathFinder;
  private final DistanceCalculator distanceCalculator;
  // Keyed by the two cells packed into a long (see cellPairKey): the map grows
  // with every (vessel cell, destination) pair valued, and object keys made
  // lookups a sixth of a NW Med run.
  private final Long2DoubleMap pathLengthsInKm = new Long2DoubleOpenHashMap();

  /**
   * @param engine             the engine whose speed gives the route's duration
   * @param pathFinder         finds the paths of the route's two legs
   * @param distanceCalculator measures the paths
   */
  RouteViaDestination(
    final VesselComponent<? extends Engine> engine,
    final GridPathFinder pathFinder,
    final DistanceCalculator distanceCalculator
  ) {
    this.engine = checkNotNull(engine);
    this.pathFinder = checkNotNull(pathFinder);
    this.distanceCalculator = checkNotNull(distanceCalculator);
  }

  /**
   * @param vessel      the vessel that would sail the route
   * @param destination the cell to fish at
   * @return the route from the vessel's current cell to {@code destination},
   *         then back to its home port
   * @throws IllegalStateException if either leg has no path
   */
  @Override
  public Route apply(final Vessel vessel, final Int2D destination) {
    final double distanceInKm =
      distanceInKm(vessel.getCell(), destination) +
      distanceInKm(destination, vessel.getHomePortLocation());
    return new Route(
      distanceInKm,
      DistanceCalculator.travelDuration(
        distanceInKm,
        engine.of(vessel).getCruisingSpeedInKph()
      )
    );
  }

  private double distanceInKm(final Int2D start, final Int2D end) {
    return pathLengthsInKm.computeIfAbsent(cellPairKey(start, end), _ ->
      distanceCalculator.distanceInKm(
        pathFinder
          .getPath(start, end)
          .orElseThrow(() ->
            new IllegalStateException("No path from " + start + " to " + end)
          )
      )
    );
  }

  // Each coordinate in 16 bits: grids are far smaller than 65,536 cells a side.
  private static long cellPairKey(final Int2D start, final Int2D end) {
    return (
      ((long) coordinate(start.x) << 48) |
      ((long) coordinate(start.y) << 32) |
      ((long) coordinate(end.x) << 16) |
      coordinate(end.y)
    );
  }

  private static int coordinate(final int value) {
    checkArgument(
      value >= 0 && value < 1 << 16,
      "Cell coordinate out of range: %s.",
      value
    );
    return value;
  }
}
