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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.nio.file.Path;
import java.util.Optional;

import static java.lang.System.Logger.Level.ERROR;
import static java.util.Comparator.comparingDouble;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PortGridFromFileFactory extends GlobalScopeFactory<PortGrid> {

    private static final System.Logger logger =
        System.getLogger(PortGridFromFileFactory.class.getName());

    private Factory<? extends BathymetricGrid> bathymetricGrid;
    private Factory<? extends DistanceCalculator> distanceCalculator;
    private Factory<? extends Path> path;
    private String idColumn;
    private String nameColumn;
    private String longitudeColumn;
    private String latitudeColumn;

    @Override
    protected PortGrid newInstance(final Simulation simulation) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(simulation);
        final DistanceCalculator distanceCalculator = this.distanceCalculator.get(simulation);
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
            final Port port = new Port(
                row.getString(idColumn),
                row.getString(nameColumn)
            );
            coordinateToCell(
                bathymetricGrid,
                coordinate,
                distanceCalculator
            ).ifPresentOrElse(
                cell -> sparseGrid2D.setObjectLocation(port, cell),
                () -> logger.log(
                    ERROR,
                    () -> "No suitable land cell found for " + port + " near " + coordinate + "."
                )
            );

        });
        return new PortGrid(bathymetricGrid, sparseGrid2D);
    }

    private Optional<Int2D> coordinateToCell(
        final BathymetricGrid bathymetricGrid,
        final Coordinate coordinate,
        final DistanceCalculator distanceCalculator
    ) {
        final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
        final Int2D cell = modelGrid.toCell(coordinate);
        if (validPortLocation(bathymetricGrid, cell)) {
            return Optional.of(cell);
        } else {
            return bathymetricGrid
                .getActiveLandNeighbours(cell)
                .min(comparingDouble(landNeighbour ->
                    distanceCalculator.distanceInKm(
                        coordinate,
                        modelGrid.toCoordinate(landNeighbour)
                    ))
                );
        }
    }

    private boolean validPortLocation(
        final BathymetricGrid bathymetricGrid,
        final Int2D cell
    ) {
        return bathymetricGrid.getModelGrid().isInGrid(cell) &&
            bathymetricGrid.isLand(cell) &&
            bathymetricGrid.getWaterNeighbours(cell).findAny().isPresent();
    }

}
