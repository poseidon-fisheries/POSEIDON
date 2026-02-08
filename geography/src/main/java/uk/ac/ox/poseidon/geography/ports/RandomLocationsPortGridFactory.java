/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toCollection;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RandomLocationsPortGridFactory extends SimulationScopeFactory<PortGrid> {

    private Factory<? super SimulationScope, ? extends BathymetricGrid> bathymetricGrid;
    private Factory<? super SimulationScope, ? extends Supplier<String>> idSupplier;
    private int numberOfPorts;
    private int minimumAdjacentWaterTiles;

    @Override
    protected PortGrid newInstance(final SimulationScope scope) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(scope);
        final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
        final List<Int2D> suitableTiles =
            bathymetricGrid
                .getLandCells()
                .filter(cell ->
                    modelGrid
                        .getActiveNeighbours(cell)
                        .stream()
                        .filter(bathymetricGrid::isWater)
                        .count() >= minimumAdjacentWaterTiles
                )
                .collect(toCollection(ArrayList::new)); // because we are going to shuffle it
        checkState(
            suitableTiles.size() >= numberOfPorts,
            "Only %s suitable land tiles for %s ports.",
            suitableTiles.size(),
            numberOfPorts
        );
        Collections.shuffle(
            suitableTiles,
            new Random(scope.getSimulation().random.nextLong())
        );
        final SparseGrid2D sparseGrid2D =
            new SparseGrid2D(
                modelGrid.getGridWidth(),
                modelGrid.getGridHeight()
            );
        final MutablePortGrid portGrid = new MutablePortGrid(bathymetricGrid, sparseGrid2D);
        final Supplier<String> idSupplier = this.idSupplier.get(scope);
        suitableTiles
            .stream()
            .limit(numberOfPorts)
            .forEach(cell -> {
                final String portCode = idSupplier.get();
                portGrid.createPort(portCode, "Port " + portCode, cell);
            });
        return portGrid;
    }
}
