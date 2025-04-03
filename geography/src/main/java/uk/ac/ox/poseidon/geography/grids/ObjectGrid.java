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

package uk.ac.ox.poseidon.geography.grids;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import lombok.Getter;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.MasonUtils;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static lombok.AccessLevel.PRIVATE;
import static uk.ac.ox.poseidon.core.MasonUtils.bagToStream;

public abstract class ObjectGrid<T>
    extends AbstractGrid<SparseGrid2D>
    implements Grid<SparseGrid2D>, Iterable<T> {

    @Getter(value = PRIVATE, lazy = true)
    private final ImmutableMap<String, T> objectsById =
        Streams.stream(iterator()).collect(toImmutableMap(this::getObjectId, o -> o));

    protected ObjectGrid(
        final ModelGrid modelGrid
    ) {
        super(modelGrid, new SparseGrid2D(modelGrid.getGridWidth(), modelGrid.getGridHeight()));
    }

    protected ObjectGrid(
        final ModelGrid modelGrid,
        final SparseGrid2D field
    ) {
        super(modelGrid, field);
    }

    public Int2D getLocation(final T object) {
        return field.getObjectLocation(object);
    }

    public Stream<T> getObjectsAt(final Int2D cell) {
        return bagToStream(
            field.getObjectsAtLocation(cell.x, cell.y)
        );
    }

    public boolean anyObjectsAt(final Int2D cell) {
        return numObjectsAt(cell) > 0;
    }

    public int numObjectsAt(final Int2D cell) {
        return field.numObjectsAtLocation(cell.x, cell.y);
    }

    @Override
    @Nonnull
    public Iterator<T> iterator() {
        return MasonUtils.<T>bagToStream(field.allObjects).iterator();
    }

    public Stream<T> stream() {
        return Streams.stream(iterator());
    }

    protected abstract String getObjectId(T object);

    public Optional<T> getObject(final String id) {
        return Optional.ofNullable(getObjectsById().get(id));
    }

}
