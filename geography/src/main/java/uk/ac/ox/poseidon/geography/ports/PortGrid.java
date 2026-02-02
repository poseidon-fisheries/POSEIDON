/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

public interface PortGrid extends Iterable<Port> {

    Stream<Port> getPorts();

    Int2D getLocation(Port port);

    Stream<Port> getObjectsAt(Int2D cell);

    boolean anyObjectsAt(Int2D cell);

    int numObjectsAt(Int2D cell);

    Optional<Port> getObject(String id);

    default void validateLocation(
        final Coordinate coordinate
    ) {
        validateLocation(getModelGrid().toCell(coordinate), coordinate);
    }

    default void validateLocation(
        final Int2D cell
    ) {
        validateLocation(cell, getModelGrid().toCoordinate(cell));
    }

    default void validateLocation(
        final Int2D cell,
        final Coordinate coordinate
    ) {
        checkState(
            getBathymetricGrid().isLand(cell),
            "Invalid port location: cell %s (coordinate %s) is not on land.",
            cell, coordinate
        );
        checkState(
            getBathymetricGrid().getActiveWaterNeighbours(cell).findAny().isPresent(),
            "Invalid location: coordinate cell %s (coordinate %s) does not have any active water " +
                "neighbors.",
            cell, coordinate
        );
    }

    BathymetricGrid getBathymetricGrid();

    default ModelGrid getModelGrid() {
        return getBathymetricGrid().getModelGrid();
    }
}
