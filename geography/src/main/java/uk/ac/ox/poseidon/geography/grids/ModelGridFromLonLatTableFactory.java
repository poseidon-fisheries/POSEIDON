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

package uk.ac.ox.poseidon.geography.grids;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.utils.LonLatTable;

/**
 * A {@link RelativeScopeFactory} for a {@link ModelGrid} sized to fit a
 * resolved {@link LonLatTable}'s coordinates, padded by a fixed margin: the
 * envelope is the coordinates' bounding box expanded by
 * {@code mapPaddingInDegrees} on every side, and the grid height is derived
 * from {@code gridWidthInCells} to match the envelope's aspect ratio. Built via
 * {@link Factories#modelGridFromLonLatTable(Factory, int, double)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class ModelGridFromLonLatTableFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  ModelGrid
> {

  private final Factory<? super S, ? extends LonLatTable> lonLatTable;
  private final int gridWidthInCells;
  private final double mapPaddingInDegrees;

  @Override
  protected ModelGrid newInstance(final S scope) {
    final List<Coordinate> coordinates = lonLatTable
      .get(scope)
      .coordinateStream()
      .toList();

    final DoubleSummaryStatistics longitudeStats = coordinates
      .stream()
      .mapToDouble(Coordinate::getLon)
      .summaryStatistics();

    final DoubleSummaryStatistics latitudeStats = coordinates
      .stream()
      .mapToDouble(Coordinate::getLat)
      .summaryStatistics();

    final Envelope envelope = new Envelope(
      longitudeStats.getMin() - mapPaddingInDegrees,
      longitudeStats.getMax() + mapPaddingInDegrees,
      latitudeStats.getMin() - mapPaddingInDegrees,
      latitudeStats.getMax() + mapPaddingInDegrees
    );
    final double heightToWidth = envelope.getHeight() / envelope.getWidth();
    final int gridHeightInCells = (int) Math.round(
      gridWidthInCells * heightToWidth
    );

    return ModelGrid.create(gridWidthInCells, gridHeightInCells, envelope);
  }
}
