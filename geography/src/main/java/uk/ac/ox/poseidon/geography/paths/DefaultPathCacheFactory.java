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

package uk.ac.ox.poseidon.geography.paths;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A {@link RelativeScopeFactory} for a {@link DefaultPathCache}, built via
 * {@link Factories#pathCache(Factory, Factory, Factory)}. {@link #newInstance} builds a bare,
 * empty {@link DefaultPathCache} regardless of the resolved dependencies — {@code bathymetricGrid},
 * {@code portGrid}, and {@code distance} are never read there. They exist purely to shape this
 * factory's identity: {@code @Data}-generated {@code equals}/{@code hashCode} include them, and
 * {@link uk.ac.ox.poseidon.core.AbstractFactory#get} caches by (scope key, this factory's
 * {@code hashCode()}), so two
 * {@code DefaultPathCacheFactory}s built from the <em>same</em> grid/port/distance dependencies
 * resolve to the <em>same</em> cache instance (correctly sharing cached paths), while factories
 * built from different dependencies get distinct caches. Don't "simplify" this by dropping the
 * unused fields — that would collapse every path cache in a scenario into one shared instance
 * regardless of which grids it was actually built for.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DefaultPathCacheFactory<S extends Scope>
    extends RelativeScopeFactory<S, PathCache<Int2D>> {

    private Factory<? super S, ? extends BathymetricGrid> bathymetricGrid;
    private Factory<? super S, ? extends PortGrid> portGrid;
    private Factory<? super S, ? extends DistanceCalculator> distance;

    @Override
    protected PathCache<Int2D> newInstance(final S scope) {
        return new DefaultPathCache<>();
    }
}
