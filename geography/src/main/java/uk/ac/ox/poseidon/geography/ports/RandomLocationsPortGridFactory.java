/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toCollection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RandomLocationsPortGridFactory extends SimulationScopeFactory<PortGrid> {

    @NonNull private Factory<? extends BathymetricGrid> bathymetricGrid;
    @NonNull private Factory<? extends Port> portFactory;
    private int numberOfPorts;
    private int minimumAdjacentWaterTiles;

    @Override
    protected PortGrid newInstance(final Simulation simulation) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(simulation);
        final GridExtent gridExtent = bathymetricGrid.getGridExtent();
        final List<Int2D> suitableTiles =
            bathymetricGrid
                .getLandCells()
                .stream()
                .filter(cell ->
                    gridExtent
                        .getNeighbours(cell)
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
            new Random(simulation.random.nextLong())
        );
        final SparseGrid2D sparseGrid2D =
            new SparseGrid2D(
                gridExtent.getGridWidth(),
                gridExtent.getGridHeight()
            );
        suitableTiles
            .stream()
            .limit(numberOfPorts)
            .forEach(cell ->
                sparseGrid2D.setObjectLocation(portFactory.get(simulation), cell)
            );
        return new DefaultPortGrid(bathymetricGrid, sparseGrid2D);
    }
}
