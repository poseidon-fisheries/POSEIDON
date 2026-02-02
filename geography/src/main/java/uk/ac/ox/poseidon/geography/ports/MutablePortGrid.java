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

import lombok.Getter;
import lombok.NonNull;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.MutableGrid;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toSet;

@Getter
public class MutablePortGrid extends AbstractPortGrid implements MutableGrid<SparseGrid2D> {

    public static MutablePortGrid copyOf(final @NonNull PortGrid source) {
        final SparseGrid2D sparseGrid2D = new SparseGrid2D(
            source.getModelGrid().getGridWidth(),
            source.getModelGrid().getGridHeight()
        );
        source.getPorts()
            .forEach(port -> sparseGrid2D.setObjectLocation(port, source.getLocation(port)));
        return new MutablePortGrid(source.getBathymetricGrid(), sparseGrid2D);
    }

    public MutablePortGrid(
        final @NonNull BathymetricGrid bathymetricGrid,
        final @NonNull SparseGrid2D sparseGrid2D
    ) {
        super(bathymetricGrid.getModelGrid(), sparseGrid2D, bathymetricGrid);
    }

    public MutablePortGrid(
        final BathymetricGrid bathymetricGrid,
        final Map<Port, ? extends Coordinate> portCoordinates
    ) {
        this(
            bathymetricGrid,
            makeField(bathymetricGrid, portCoordinates)
        );
    }

    Port createPort(
        final String portCode,
        final String portName,
        final Coordinate coordinate
    ) {
        return createPort(portCode, portName, getModelGrid().toCell(coordinate));
    }

    Port createPort(
        final String portCode,
        final String portName,
        final Int2D cell
    ) {
        checkState(
            !getPorts().map(Port::getCode).collect(toSet()).contains(portCode),
            "Port code %s already exists", portCode
        );
        validateLocation(cell);
        final Port port = new Port(portCode, portName, cell);
        field.setObjectLocation(port, cell);
        return port;
    }

    @Override
    public SparseGrid2D getField() {
        return field;
    }
}
