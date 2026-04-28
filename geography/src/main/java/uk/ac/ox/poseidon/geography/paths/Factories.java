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

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

public class Factories {

    private Factories() {
    }

    public static <S extends Scope> DefaultPathCacheFactory<S> pathCache(
        final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid,
        final Factory<? super S, ? extends PortGrid> portGrid,
        final Factory<? super S, ? extends DistanceCalculator> distance
    ) {
        return new DefaultPathCacheFactory<>(bathymetricGrid, portGrid, distance);
    }

    public static DefaultPathFinderFactory pathFinder(
        final Factory<? super SimulationScope, ? extends BathymetricGrid> bathymetricGrid,
        final Factory<? super SimulationScope, ? extends PortGrid> portGrid,
        final Factory<? super SimulationScope, ? extends DistanceCalculator> distance
    ) {
        return new DefaultPathFinderFactory(
            bathymetricGrid,
            portGrid,
            distance,
            pathCache(bathymetricGrid, portGrid, distance)
        );
    }
}
