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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A {@link RelativeScopeFactory} for a {@link DefaultPathCache}, built via
 * {@link Factories#pathCache(Factory, Factory, Factory)}. {@link #newInstance}
 * builds an empty {@link DefaultPathCache} over the cells of
 * {@code bathymetricGrid}'s model grid; {@code portGrid} and {@code distance}
 * are never read there. They are inputs all the same: with
 * {@code bathymetricGrid}, they give this factory its scope (one cache shared
 * by every simulation when they are global, one per simulation otherwise), and
 * a change to any of them, made by deriving a scenario, copies this factory, so
 * that the derived scenario gets a new cache rather than paths found on other
 * grids. Don't "simplify" this by dropping the unread fields — the cache would
 * then be global whatever grids it was actually built for, and outlive a
 * change to them.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class DefaultPathCacheFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  PathCache<Int2D>
> {

  private final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid;
  private final Factory<? super S, ? extends PortGrid> portGrid;
  private final Factory<? super S, ? extends DistanceCalculator> distance;

  @Override
  protected PathCache<Int2D> newInstance(final S scope) {
    return new DefaultPathCache(bathymetricGrid.get(scope).getModelGrid());
  }
}
