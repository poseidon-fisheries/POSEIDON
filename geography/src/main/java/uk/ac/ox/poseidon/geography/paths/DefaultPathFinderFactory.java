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

package uk.ac.ox.poseidon.geography.paths;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.AbstractFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultPathFinderFactory<S extends Scope>
    extends AbstractFactory<S, GridPathFinder> {

    /* TODO: the DefaultPathFinderFactory currently needs to be SimulationScope because
        it relies on the port grid, which is currently also simulation-scope (because of
        random port locations, and also because we currently allow adding ports after the
        creation of the grid. This is not great because it prevents us from being to share
        the cache from the AStarPathfinder across simulations. I need to think about some
        way of making an immutable port grid that can be shared across simulations, or
        to make the path finder not depend on the port grid at all.
        ...
        Update: I think that with the setup that we now have, this can be resolved statically:
        If we pass a simulation scope port grid (like the random one), this will automatically
        be simulation scope (as it should) but if we pass a global scope port grid, it can
        be global scope and we get the caching.
     */
    private Factory<? super S, ? extends BathymetricGrid> bathymetricGrid;
    private Factory<? super S, ? extends PortGrid> portGrid;
    private Factory<? super S, ? extends DistanceCalculator> distance;

    @Override
    protected GridPathFinder newInstance(final S scope) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(scope);
        final PortGrid portGrid = this.portGrid.get(scope);
        return new CachingGridPathFinder(
            new FallbackGridPathfinder(
                new BresenhamPathFinder(
                    bathymetricGrid,
                    portGrid
                ),
                new AStarPathFinder(
                    bathymetricGrid,
                    portGrid,
                    distance.get(scope)
                )
            ),
            new DefaultPathCache<>()
        );
    }

}
