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

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static lombok.AccessLevel.PACKAGE;

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
 * search centre, asked once on each pick. An absent centre gives no option.
 * Otherwise it grows the search radius until a cell satisfies the criteria
 * for the vessel. The radius grows by at most {@code maximumWidening} cells
 * beyond the initial one. Widening stops at the grid's larger dimension; a
 * supplied radius already beyond that is searched once. An unbounded search
 * would otherwise test every water cell when none qualifies.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class NearbyCellPicker implements Function<Vessel, Optional<Cell>> {

  private final GridPathFinder pathFinder;
  private final BiFunction<
    ? super Vessel,
    ? super Cell,
    Boolean
  > optionEligibilityCriteria;
  private final Function<? super Vessel, Integer> searchRadius;
  private final int maximumWidening;
  private final Function<? super Vessel, ? extends Optional<Cell>> searchCentre;
  private final MersenneTwisterFast rng;

  /**
   * @param vessel the vessel to pick for
   * @return a uniformly random accessible water cell satisfying the option
   *         predicate, within the smallest searched neighbourhood that has any;
   *         an empty optional if none qualify within the largest neighbourhood
   *         searched
   */
  @Override
  public Optional<Cell> apply(final Vessel vessel) {
    final Optional<Cell> suppliedCentre = searchCentre.apply(vessel);
    Optional<Cell> result = Optional.empty();
    if (suppliedCentre.isPresent()) {
      final int initialSearchRadius = searchRadius.apply(vessel);
      checkArgument(
        initialSearchRadius >= 0,
        "searchRadius must not be negative."
      );
      checkArgument(
        maximumWidening >= 0,
        "maximumWidening must not be negative."
      );
      final int gridExtent = max(
        pathFinder.getModelGrid().getGridWidth(),
        pathFinder.getModelGrid().getGridHeight()
      );
      final int maximumSearchRadius =
        initialSearchRadius +
        min(maximumWidening, max(0, gridExtent - initialSearchRadius));
      result = IntStream.rangeClosed(initialSearchRadius, maximumSearchRadius)
        .mapToObj(searchRadius ->
          pickWithin(
            vessel,
            suppliedCentre.orElseThrow(),
            searchRadius,
            searchRadius == initialSearchRadius
          )
        )
        .flatMap(Optional::stream)
        .findFirst();
    }
    return result;
  }

  /**
   * @return a uniformly random accessible water cell satisfying the option
   *         predicate, among those {@code searchRadius} cells away from
   *         {@code centre}, or closer if {@code isInitial}
   */
  private Optional<Cell> pickWithin(
    final Vessel vessel,
    final Cell centre,
    final int searchRadius,
    final boolean isInitial
  ) {
    final List<Cell> neighbours = pathFinder.getAccessibleWaterNeighbours(
      centre,
      searchRadius
    );
    // Every cell of the previous neighbourhood has already failed the
    // predicate
    final List<Cell> candidates = isInitial
      ? neighbours
      : ring(neighbours, centre, searchRadius);
    return RandomPicker.pickFrom(
      vessel,
      candidates,
      optionEligibilityCriteria,
      rng
    );
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
          max(
            abs(cell.getX() - centre.getX()),
            abs(cell.getY() - centre.getY())
          ) == size
      )
      .toList();
  }
}
