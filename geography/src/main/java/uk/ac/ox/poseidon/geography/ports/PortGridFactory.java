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
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.utils.Pair;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.Comparator.comparingDouble;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PortGridFactory<S extends Scope>
    extends RelativeScopeFactory<S, ImmutablePortGrid> {

    private static final System.Logger logger =
        System.getLogger(PortGridFactory.class.getName());

    private Factory<? super S, ? extends List<Pair<Port, Coordinate>>> ports;
    private Factory<? super S, ? extends BathymetricGrid> bathymetricGrid;
    private Factory<? super S, ? extends DistanceCalculator> distanceCalculator;

    @Override
    protected ImmutablePortGrid newInstance(final S scope) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(scope);
        final DistanceCalculator distanceCalculator = this.distanceCalculator.get(scope);
        final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
        final SparseGrid2D sparseGrid2D =
            new SparseGrid2D(
                modelGrid.getGridWidth(),
                modelGrid.getGridHeight()
            );
        ports.get(scope).forEach(pair -> {
            final Port port = pair.getFirst();
            final Coordinate coordinate = pair.getSecond();
            final Int2D cell = coordinateToCell(
                bathymetricGrid,
                coordinate,
                distanceCalculator
            ).orElseThrow(() -> new RuntimeException(format(
                "No suitable land cell found for port {0} near {1}",
                port, coordinate
            )));
            sparseGrid2D.setObjectLocation(port, cell);
        });
        return new ImmutablePortGrid(sparseGrid2D, bathymetricGrid);
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
