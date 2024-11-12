/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.agents.behaviours.destination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RandomDestinationSupplierFactory
    extends VesselScopeFactory<DestinationSupplier> {

    private Factory<? extends BathymetricGrid> bathymetricGrid;
    private Factory<? extends PathFinder<Int2D>> pathFinder;

    @Override
    protected DestinationSupplier newInstance(
        Simulation simulation,
        Vessel vessel
    ) {
        PathFinder<Int2D> pathFinder = this.pathFinder.get(simulation);
        return new RandomDestinationSupplier(
            bathymetricGrid
                .get(simulation)
                .getWaterCells()
                .stream()
                .filter(cell -> pathFinder.getPath(vessel.getCurrentCell(), cell).isPresent())
                .collect(toImmutableList()),
            simulation.random
        );
    }
}
