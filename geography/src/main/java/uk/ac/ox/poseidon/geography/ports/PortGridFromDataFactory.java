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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.Comparator.comparingDouble;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PortGridFromDataFactory extends SimulationScopeFactory<PortGrid> {

    private static final System.Logger logger =
        System.getLogger(PortGridFromDataFactory.class.getName());

    private Factory<? super SimulationScope, ? extends Table> data;
    private Factory<? super SimulationScope, ? extends BathymetricGrid> bathymetricGrid;
    private Factory<? super SimulationScope, ? extends DistanceCalculator> distanceCalculator;
    private String portCodeColumn;
    private String nameColumn;
    private String longitudeColumn;
    private String latitudeColumn;

    @Override
    protected PortGrid newInstance(final SimulationScope scope) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(scope);
        final DistanceCalculator distanceCalculator = this.distanceCalculator.get(scope);
        final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
        final SparseGrid2D sparseGrid2D =
            new SparseGrid2D(
                modelGrid.getGridWidth(),
                modelGrid.getGridHeight()
            );
        data.get(scope).forEach(row -> {
            final String portCode = row.getString(portCodeColumn);
            final String portName = row.getString(nameColumn);
            final Coordinate coordinate = new Coordinate(
                row.getDouble(longitudeColumn),
                row.getDouble(latitudeColumn)
            );
            final Int2D cell = coordinateToCell(
                bathymetricGrid,
                coordinate,
                distanceCalculator
            ).orElseThrow(() -> new RuntimeException(format(
                "No suitable land cell found for port {0} ({1}) near {2}",
                portName, portCode, coordinate
            )));
            final Port port = new Port(portCode, portName, cell);
            sparseGrid2D.setObjectLocation(port, cell);
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
