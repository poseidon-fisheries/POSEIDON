/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.geography.ports;

import lombok.*;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.nio.file.Path;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PortGridFromFileFactory extends GlobalScopeFactory<DefaultPortGrid> {

    @NonNull private Factory<? extends BathymetricGrid> bathymetricGrid;
    @NonNull private Factory<? extends Path> path;
    @NonNull private String idColumn;
    @NonNull private String nameColumn;
    @NonNull private String longitudeColumn;
    @NonNull private String latitudeColumn;

    @Override
    protected DefaultPortGrid newInstance(final Simulation simulation) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(simulation);
        final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
        final SparseGrid2D sparseGrid2D =
            new SparseGrid2D(
                modelGrid.getGridWidth(),
                modelGrid.getGridHeight()
            );
        Table.read().file(path.get(simulation).toFile()).forEach(row -> {
            final Coordinate coordinate = new Coordinate(
                row.getDouble(longitudeColumn),
                row.getDouble(latitudeColumn)
            );
            final SimplePort port = new SimplePort(
                row.getString(idColumn),
                row.getString(nameColumn)
            );
            final Int2D cell = modelGrid.toCell(coordinate);
            // checkCell(cell, coordinate, portName, bathymetricGrid);
            sparseGrid2D.setObjectLocation(port, cell);
        });
        return new DefaultPortGrid(bathymetricGrid, sparseGrid2D);
    }
}
