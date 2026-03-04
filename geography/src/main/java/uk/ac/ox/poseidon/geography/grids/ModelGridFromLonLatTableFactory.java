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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.utils.LonLatTable;

import java.util.DoubleSummaryStatistics;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelGridFromLonLatTableFactory<S extends Scope>
    extends RelativeScopeFactory<S, ModelGrid> {

    private Factory<? super S, ? extends LonLatTable> lonLatTable;
    private int gridWidthInCells;
    private double mapPaddingInDegrees;

    @Override
    protected ModelGrid newInstance(final S scope) {

        final List<Coordinate> coordinates = lonLatTable.get(scope).coordinateStream().toList();

        final DoubleSummaryStatistics longitudeStats =
            coordinates.stream().mapToDouble(Coordinate::getLon).summaryStatistics();

        final DoubleSummaryStatistics latitudeStats =
            coordinates.stream().mapToDouble(Coordinate::getLat).summaryStatistics();

        final Envelope envelope = new Envelope(
            longitudeStats.getMin() - mapPaddingInDegrees,
            longitudeStats.getMax() + mapPaddingInDegrees,
            latitudeStats.getMin() - mapPaddingInDegrees,
            latitudeStats.getMax() + mapPaddingInDegrees
        );
        final double heightToWidth = envelope.getHeight() / envelope.getWidth();
        final int gridHeightInCells = (int) Math.round(gridWidthInCells * heightToWidth);

        return ModelGrid.create(gridWidthInCells, gridHeightInCells, envelope);
    }
}
