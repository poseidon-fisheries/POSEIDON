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

package uk.ac.ox.poseidon.fishing.choices;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

import ec.util.MersenneTwisterFast;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

/**
 * Picks, for a vessel, a random accessible water cell in the neighbourhood of a
 * search centre, asked for anew on each pick (the vessel's own cell if it gives
 * none), growing the search radius until a cell satisfies the option predicate
 * for the vessel. The radius grows by at most {@code maximumWidening} cells
 * beyond the initial one, and never beyond the grid's larger dimension: when no
 * cell is legal anywhere, which is common (a closed port, no time left), an
 * unbounded search would test every water cell of the grid.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class NeighbourhoodCellPicker implements Function<Vessel, Cell> {

  private final GridPathFinder pathFinder;
  private final BiFunction<
    ? super Vessel,
    ? super Cell,
    Boolean
  > optionPredicate;
  private final Function<? super Vessel, Integer> neighbourhoodSize;
  private final int maximumWidening;
  private final Function<? super Vessel, ? extends Cell> searchCentre;
  private final MersenneTwisterFast rng;

  /**
   * @param vessel the vessel to pick for
   * @return a uniformly random accessible water cell satisfying the option
   *         predicate, within the smallest searched neighbourhood that has any;
   *         {@code null} if none qualify within the largest neighbourhood
   *         searched
   */
  @Override
  public Cell apply(final Vessel vessel) {
    final int initialNeighbourhoodSize = neighbourhoodSize.apply(vessel);
    final int gridExtent = max(
      pathFinder.getModelGrid().getGridWidth(),
      pathFinder.getModelGrid().getGridHeight()
    );
    final int maxNeighbourhoodSize =
      initialNeighbourhoodSize +
      min(maximumWidening, gridExtent - initialNeighbourhoodSize);
    final Cell suppliedCentre = searchCentre.apply(vessel);
    final Cell centre =
      suppliedCentre != null ? suppliedCentre : vessel.getCell();
    return IntStream.rangeClosed(initialNeighbourhoodSize, maxNeighbourhoodSize)
      .mapToObj(neighbourhoodSize ->
        pickWithin(
          vessel,
          centre,
          neighbourhoodSize,
          neighbourhoodSize == initialNeighbourhoodSize
        )
      )
      .flatMap(Optional::stream)
      .findFirst()
      .orElse(null);
  }

  /**
   * @return a uniformly random accessible water cell satisfying the option
   *         predicate, among those {@code neighbourhoodSize} cells away from
   *         {@code centre}, or closer if {@code isInitial}
   */
  private Optional<Cell> pickWithin(
    final Vessel vessel,
    final Cell centre,
    final int neighbourhoodSize,
    final boolean isInitial
  ) {
    final List<Cell> neighbours = pathFinder.getAccessibleWaterNeighbours(
      centre,
      neighbourhoodSize
    );
    // Every cell of the previous neighbourhood has already failed the
    // predicate
    final List<Cell> candidates = isInitial
      ? neighbours
      : ring(neighbours, centre, neighbourhoodSize);
    return shuffledStream(candidates, rng)
      .filter(cell -> optionPredicate.apply(vessel, cell))
      .findFirst();
  }

  /**
   * @return the cells of {@code neighbours} exactly {@code size} cells away
   *         from the centre
   */
  private static List<Cell> ring(
    final List<Cell> neighbours,
    final Cell centre,
    final int size
  ) {
    return neighbours
      .stream()
      .filter(
        cell ->
          max(abs(cell.x() - centre.x()), abs(cell.y() - centre.y())) == size
      )
      .toList();
  }
}
