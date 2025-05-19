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

package uk.ac.ox.poseidon.geography.ports;

import lombok.*;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.utils.IdSupplier;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PortGridFromLocationsFactory extends GlobalScopeFactory<PortGrid> {

    @NonNull private Factory<? extends IdSupplier> idSupplier;
    @NonNull private Factory<? extends BathymetricGrid> bathymetricGrid;
    @NonNull private Map<String, Factory<? extends Coordinate>> ports;

    @Override
    protected PortGrid newInstance(final Simulation simulation) {
        final IdSupplier idSupplier = this.idSupplier.get(simulation);
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(simulation);
        final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
        final SparseGrid2D sparseGrid2D =
            new SparseGrid2D(
                modelGrid.getGridWidth(),
                modelGrid.getGridHeight()
            );
        ports.forEach((portName, coordinateFactory) -> {
            final Coordinate coordinate = coordinateFactory.get(simulation);
            final Port port = new Port(idSupplier.nextId(), portName);
            final Int2D cell = modelGrid.toCell(coordinate);
            checkCell(cell, coordinate, portName, bathymetricGrid);
            sparseGrid2D.setObjectLocation(port, cell);
        });
        return new PortGrid(bathymetricGrid, sparseGrid2D);
    }

    private void checkCell(
        final Int2D cell,
        final Coordinate coordinate,
        final String portName,
        final BathymetricGrid bathymetricGrid
    ) {
        checkState(
            bathymetricGrid.isLand(cell),
            "Coordinate " + coordinate + " for port " + portName + " is not on land."
        );
        checkState(
            bathymetricGrid.getActiveWaterNeighbours(cell).findAny().isPresent(),
            "Coordinate " +
                coordinate +
                " for port " +
                portName +
                " does not have any active water neighbors."
        );
    }

}
