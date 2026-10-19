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

package uk.ac.ox.poseidon.geography.paths;

import static com.google.common.base.Preconditions.checkNotNull;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

/**
 * How far apart two cells are by sea: the length of the path its path finder
 * gives from one cell to the other, as its distance calculator measures it.
 * That is the path the path finder chooses, not necessarily the shortest
 * distance by sea.
 * <p>
 * Each length is remembered after it is first measured, for every pair of
 * cells asked about, without bound: it depends only on the two cells, since
 * the path finder gives the same path for them throughout a run. Lengths are
 * directional: the length from one cell to another is measured and remembered
 * apart from the length back, which a path finder may find different.
 * <p>
 * Whether one cell can be reached from another is not a question this class
 * answers: ask the path finder ({@link GridPathFinder#isAccessible}). Asking
 * for the length between two cells that are not connected is an error.
 */
public class PathLengthCalculator {

  private final GridPathFinder pathFinder;
  private final DistanceCalculator distanceCalculator;
  // Keyed by the two cells' ModelGrid.cellPairKey: the map grows with every
  // pair of cells asked about, and object keys made lookups a sixth of a
  // NW Med run. It is not safe to use from several threads, which is why
  // its factory builds one per simulation rather than sharing it.
  private final Long2DoubleMap lengthsInKm = new Long2DoubleOpenHashMap();

  /**
   * @param pathFinder         finds the path between two cells
   * @param distanceCalculator measures the paths
   */
  public PathLengthCalculator(
    final GridPathFinder pathFinder,
    final DistanceCalculator distanceCalculator
  ) {
    this.pathFinder = checkNotNull(pathFinder);
    this.distanceCalculator = checkNotNull(distanceCalculator);
  }

  /**
   * @param start the cell to measure from
   * @param end   the cell to measure to
   * @return the length of the path from {@code start} to {@code end}, in
   *         kilometres; zero when they are the same cell
   * @throws IllegalArgumentException if there is no path from {@code start} to
   *                                  {@code end}, or if either cell is
   *                                  outside the path finder's grid
   */
  public double lengthInKm(final Int2D start, final Int2D end) {
    return lengthsInKm.computeIfAbsent(
      pathFinder.getModelGrid().cellPairKey(start, end),
      _ ->
        distanceCalculator.distanceInKm(
          pathFinder
            .getPath(start, end)
            .orElseThrow(() ->
              new IllegalArgumentException(
                "No path from " + start + " to " + end
              )
            )
        )
    );
  }
}
