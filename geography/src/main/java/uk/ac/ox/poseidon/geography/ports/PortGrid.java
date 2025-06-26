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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.NonNull;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.ObjectGrid;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.poseidon.core.MasonUtils.bagToStream;

@Getter
public class PortGrid extends ObjectGrid<Port> {

    @NonNull
    private final BathymetricGrid bathymetricGrid;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PortGrid(
        final @NonNull BathymetricGrid bathymetricGrid,
        final @NonNull SparseGrid2D sparseGrid2D
    ) {
        super(bathymetricGrid.getModelGrid(), sparseGrid2D);
        this.bathymetricGrid = bathymetricGrid;
    }

    public PortGrid(
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
    protected String getObjectId(final Port port) {
        return port.getCode();
    }

    public Stream<Port> getPorts() {
        return bagToStream(field.getAllObjects());
    }

    public void validateLocation(
        final Coordinate coordinate
    ) {
        final Int2D cell = getModelGrid().toCell(coordinate);
        checkState(
            bathymetricGrid.isLand(cell),
            "Invalid port location: coordinate %s is not on land.",
            coordinate
        );
        checkState(
            bathymetricGrid.getActiveWaterNeighbours(cell).findAny().isPresent(),
            "Invalid location: coordinate %s does not have any active water neighbors.",
            coordinate
        );
    }

}
