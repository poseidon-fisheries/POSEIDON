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

import lombok.*;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultPathFinderFactory extends GlobalScopeFactory<GridPathFinder> {

    private Factory<? extends BathymetricGrid> bathymetricGrid;
    private Factory<? extends PortGrid> portGrid;
    private Factory<? extends DistanceCalculator> distance;

    @Override
    protected GridPathFinder newInstance(final @NonNull Simulation simulation) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(simulation);
        final PortGrid portGrid = this.portGrid.get(simulation);
        return new CachingGridPathFinder(
            new FallbackGridPathfinder(
                new BresenhamPathFinder(
                    bathymetricGrid,
                    portGrid
                ),
                new AStarPathFinder(
                    bathymetricGrid,
                    portGrid,
                    distance.get(simulation)
                )
            ),
            new DefaultPathCache<>()
        );
    }

}
