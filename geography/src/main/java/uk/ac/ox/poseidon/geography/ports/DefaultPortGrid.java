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

import com.vividsolutions.jts.geom.Coordinate;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.MasonUtils;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.AbstractGrid;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class DefaultPortGrid extends AbstractGrid<SparseGrid2D> implements PortGrid {

    private final SparseGrid2D sparseGrid2D;

    public DefaultPortGrid(
        final BathymetricGrid bathymetricGrid,
        final SparseGrid2D sparseGrid2D
    ) {
        super(bathymetricGrid.getGridExtent(), sparseGrid2D);
        this.sparseGrid2D = sparseGrid2D;
    }

    public DefaultPortGrid(
        final BathymetricGrid bathymetricGrid,
        final Map<Port, ? extends Coordinate> portCoordinates
    ) {
        this(
            bathymetricGrid,
            makePortGrid(bathymetricGrid, portCoordinates)
        );
    }

    private static SparseGrid2D makePortGrid(
        final BathymetricGrid bathymetricGrid,
        final Map<? extends Port, ? extends Coordinate> portCoordinates
    ) {
        final GridExtent gridExtent = bathymetricGrid.getGridExtent();
        final SparseGrid2D grid = new SparseGrid2D(
            gridExtent.getGridWidth(),
            gridExtent.getGridHeight()
        );
        portCoordinates.forEach((port, coordinate) -> {
            final Int2D cell = gridExtent.toCell(coordinate);
            checkArgument(
                bathymetricGrid.isLand(cell),
                "Port %s at coordinate %s is on water.",
                port.getName(),
                coordinate
            );
            checkArgument(
                gridExtent
                    .getNeighbours(cell)
                    .stream()
                    .anyMatch(bathymetricGrid::isWater),
                "Port %s at coordinate %s is not adjacent to water.",
                port.getName(),
                coordinate
            );
            grid.setObjectLocation(port, cell);
        });
        return grid;
    }

    @Override
    public Int2D getLocation(final Port port) {
        return sparseGrid2D.getObjectLocation(port);
    }

    @Override
    public Stream<Port> getPortsAt(final Int2D cell) {
        return MasonUtils.bagToStream(
            sparseGrid2D.getObjectsAtLocation(cell.x, cell.y)
        );
    }

    @Override
    public Stream<Port> getAllPorts() {
        return MasonUtils.bagToStream(sparseGrid2D.allObjects);
    }

}
