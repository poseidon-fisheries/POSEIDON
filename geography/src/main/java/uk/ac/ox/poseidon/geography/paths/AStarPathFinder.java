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

package uk.ac.ox.poseidon.geography.paths;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A {@link GridPathFinder} using the A* search algorithm (libGDX's
 * {@link IndexedAStarPathFinder}, via {@link GridAdaptor}), guided by a
 * resolved {@link DistanceCalculator} as its heuristic: complete (finds a path
 * whenever one exists) but slower than {@link BresenhamPathFinder}. Not built
 * standalone — see {@link DefaultPathFinderFactory} for the combination this
 * package exposes.
 */
public class AStarPathFinder extends AbstractGridPathFinder {

  private final GridAdaptor gridAdaptor;
  private final IndexedAStarPathFinder<Cell> pathFinder;
  private final Heuristic<Cell> heuristic;

  /**
   * @param bathymetricGrid    the grid to find water/land cells on
   * @param portGrid           the grid of ports, whose cells also count as
   *                           navigable
   * @param distanceCalculator the distance calculator used both as the A*
   *                           heuristic and to weight connections between cells
   */
  public AStarPathFinder(
    final BathymetricGrid bathymetricGrid,
    final PortGrid portGrid,
    final DistanceCalculator distanceCalculator
  ) {
    super(bathymetricGrid, portGrid);
    this.gridAdaptor = new GridAdaptor(
      bathymetricGrid,
      portGrid,
      distanceCalculator
    );
    this.pathFinder = new IndexedAStarPathFinder<>(gridAdaptor);
    this.heuristic = (Cell a, Cell b) ->
      (float) gridAdaptor.getDistanceCalculator().distanceInKm(a, b);
  }

  @Override
  public Optional<ImmutableList<Cell>> getPath(
    final Cell start,
    final Cell end
  ) {
    return isNavigable(start) && isNavigable(end)
      ? search(start, end)
      : Optional.empty();
  }

  private Optional<ImmutableList<Cell>> search(
    final Cell start,
    final Cell end
  ) {
    final DefaultGraphPath<Cell> path = new DefaultGraphPath<>();
    final boolean found = pathFinder.searchNodePath(
      gridAdaptor.intern(start),
      gridAdaptor.intern(end),
      heuristic,
      path
    );
    return found ? Optional.of(ImmutableList.copyOf(path)) : Optional.empty();
  }
}
