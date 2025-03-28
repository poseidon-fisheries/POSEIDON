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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.MasonUtils;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.AbstractGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static uk.ac.ox.poseidon.core.MasonUtils.bagToStream;

public class DefaultPortGrid extends AbstractGrid<SparseGrid2D> implements PortGrid {

    private final SparseGrid2D sparseGrid2D;
    private final Map<String, Port> portsById;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public DefaultPortGrid(
        final BathymetricGrid bathymetricGrid,
        final SparseGrid2D sparseGrid2D
    ) {
        super(bathymetricGrid.getModelGrid(), sparseGrid2D);
        this.sparseGrid2D = sparseGrid2D;
        this.portsById =
            MasonUtils.<Port>bagToStream(sparseGrid2D.getAllObjects())
                .collect(toImmutableMap(Port::getCode, identity()));
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
        final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
        final SparseGrid2D grid = new SparseGrid2D(
            modelGrid.getGridWidth(),
            modelGrid.getGridHeight()
        );
        portCoordinates.forEach((port, coordinate) -> {
            final Int2D cell = modelGrid.toCell(coordinate);
            checkArgument(
                bathymetricGrid.isLand(cell),
                "Port %s at coordinate %s is on water.",
                port.getName(),
                coordinate
            );
            checkArgument(
                modelGrid
                    .getActiveNeighbours(cell)
                    .stream()
                    .anyMatch(bathymetricGrid::isWater),
                "Port %s at coordinate %s is not adjacent to an active water cell.",
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
    public Port getPort(final String portId) {
        return portsById.get(portId);
    }

    @Override
    public Stream<Port> getPortsAt(final Int2D cell) {
        return bagToStream(
            sparseGrid2D.getObjectsAtLocation(cell.x, cell.y)
        );
    }

    @Override
    public boolean anyPortsAt(final Int2D cell) {
        return sparseGrid2D.numObjectsAtLocation(cell.x, cell.y) > 0;
    }

    @Override
    @Nonnull
    public Iterator<Port> iterator() {
        return bagToStream(sparseGrid2D.allObjects, Port.class).iterator();
    }
}
