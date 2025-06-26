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
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PortGridFactory extends SimulationScopeFactory<PortGrid> {

    @NonNull private Factory<? extends BathymetricGrid> bathymetricGrid;

    @Override
    protected PortGrid newInstance(final Simulation simulation) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(simulation);
        final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
        final SparseGrid2D sparseGrid2D =
            new SparseGrid2D(
                modelGrid.getGridWidth(),
                modelGrid.getGridHeight()
            );
        return new PortGrid(bathymetricGrid, sparseGrid2D);
    }

}
