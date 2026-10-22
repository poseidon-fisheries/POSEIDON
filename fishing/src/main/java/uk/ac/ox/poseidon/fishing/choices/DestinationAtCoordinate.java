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

package uk.ac.ox.poseidon.fishing.choices;

import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * A destination at the cell of a coordinate read off the vessel: its longitude
 * and latitude are values of the vessel, so that each vessel can have a spot of
 * its own.
 *
 * @param <C> the type of the vessel the coordinate is read off
 */
@RequiredArgsConstructor
public class DestinationAtCoordinate<C> implements Function<C, Optional<Cell>> {

  @NonNull
  private final ModelGrid modelGrid;

  @NonNull
  private final Function<? super C, ? extends Double> longitude;

  @NonNull
  private final Function<? super C, ? extends Double> latitude;

  /**
   * @throws IllegalArgumentException if the vessel's coordinate falls outside
   *                                  the grid
   */
  @Override
  public Optional<Cell> apply(final C vessel) {
    final Coordinate coordinate = new Coordinate(
      longitude.apply(vessel),
      latitude.apply(vessel)
    );
    modelGrid.checkIsInGrid(coordinate);
    return Optional.of(modelGrid.toCell(coordinate));
  }
}
