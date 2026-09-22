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

package uk.ac.ox.poseidon.geography.grids;

import com.google.common.collect.Streams;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.MasonUtils;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.poseidon.core.MasonUtils.bagToStream;

/**
 * An {@link AbstractGrid} placing arbitrary objects of type {@code T} on cells (a MASON
 * {@link SparseGrid2D}, so multiple objects can share a cell). Subclassed by
 * {@link uk.ac.ox.poseidon.geography.ports.AbstractPortGrid} in this module.
 */
public abstract class ObjectGrid<T>
    extends AbstractGrid<SparseGrid2D>
    implements Iterable<T> {

    /** @param modelGrid the grid this is defined over; starts empty */
    protected ObjectGrid(
        final ModelGrid modelGrid
    ) {
        super(modelGrid, new SparseGrid2D(modelGrid.getGridWidth(), modelGrid.getGridHeight()));
    }

    /**
     * @param modelGrid the grid this is defined over
     * @param field     the underlying MASON field to use directly (not copied)
     */
    protected ObjectGrid(
        final ModelGrid modelGrid,
        final SparseGrid2D field
    ) {
        super(modelGrid, field);
    }

    /** @return the cell {@code object} is placed on */
    public Int2D getLocation(final T object) {
        return field.getObjectLocation(object);
    }

    /** @return the objects placed on {@code cell} */
    public Stream<T> getObjectsAt(final Int2D cell) {
        return bagToStream(
            field.getObjectsAtLocation(cell.x, cell.y)
        );
    }

    /** @return whether any object is placed on {@code cell} */
    public boolean anyObjectsAt(final Int2D cell) {
        return numObjectsAt(cell) > 0;
    }

    /** @return the number of objects placed on {@code cell} */
    public int numObjectsAt(final Int2D cell) {
        return field.numObjectsAtLocation(cell.x, cell.y);
    }

    @Override
    @Nonnull
    public Iterator<T> iterator() {
        return MasonUtils.<T>bagToStream(field.allObjects).iterator();
    }

    /** @return every object on this grid */
    public Stream<T> stream() {
        return Streams.stream(iterator());
    }

    /**
     * @param bathymetricGrid  the bathymetric grid objects are placed relative to
     * @param portCoordinates  the ports to place, each at its given coordinate
     * @return a fresh field with one entry per {@code portCoordinates} entry
     * @throws IllegalArgumentException if any port's coordinate isn't on land, or isn't adjacent
     *                                   to an active water cell
     */
    protected static SparseGrid2D makeField(
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

    /** @return {@code object}'s unique id, used by {@link #getObject} */
    protected abstract String getObjectId(T object);

    private Map<String, T> getObjectsById() {
        return Streams.stream(iterator()).collect(toMap(this::getObjectId, o -> o));
    }

    /** @return the object with the given id, if one exists on this grid */
    public Optional<T> getObject(final String id) {
        return Optional.ofNullable(getObjectsById().get(id));
    }

    /** @return an independent copy of the underlying field */
    public SparseGrid2D copyOfField() {
        return new SparseGrid2D(field);
    }

}
