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

package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.function.Function;

public class CachePerTile<T> {

    private final LoadingCache<Int2D, T> cache;
    private final MapExtent mapExtent;

    public CachePerTile(
        final MapExtent mapExtent,
        final Function<? super Int2D, T> loaderFunction
    ) {
        this.mapExtent = mapExtent;
        this.cache = CacheBuilder.newBuilder().build(CacheLoader.from(loaderFunction::apply));
    }

    public MapExtent getMapExtent() {
        return mapExtent;
    }

    public T get(final Coordinate coordinate) {
        return get(mapExtent.coordinateToXY(coordinate));
    }

    public T get(final Double2D gridXY) {
        return get(new Int2D((int) gridXY.x, (int) gridXY.y));
    }

    public T get(final Int2D gridXY) {
        return cache.getUnchecked(gridXY);
    }

}
