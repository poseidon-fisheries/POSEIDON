/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.geography.bathymetry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BathymetricGridFromElevationValuesFactory extends GlobalScopeFactory<BathymetricGrid> {

    private Factory<? extends ModelGrid> modelGrid;
    private List<Number> elevationValues;

    @Override
    protected BathymetricGrid newInstance(final Simulation simulation) {
        final ModelGrid modelGrid = checkNotNull(this.modelGrid).get(simulation);
        final List<Number> elevationValues = checkNotNull(this.elevationValues);
        final int height = modelGrid.getGridHeight();
        final int width = modelGrid.getGridWidth();
        final int expectedCells = height * width;
        checkArgument(
            elevationValues.size() == expectedCells,
            "The model grid has %s cells but only %s elevation values were provided.",
            expectedCells, elevationValues.size()
        );
        final double[][] elevationGrid = new double[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                elevationGrid[x][y] = elevationValues.get(x + y * width).doubleValue();
            }
        }
        return new DefaultBathymetricGrid(modelGrid, elevationGrid);
    }
}
