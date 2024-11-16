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
import uk.ac.ox.poseidon.agents.behaviours.choices.Explorer;
import uk.ac.ox.poseidon.agents.behaviours.choices.RandomExplorer;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RandomGridExplorerFactory extends VesselScopeFactory<Explorer<Int2D>> {

    private Factory<? extends BathymetricGrid> bathymetricGrid;
    private Factory<? extends GridPathFinder> pathFinder;

    @Override
    protected Explorer<Int2D> newInstance(
        final Simulation simulation,
        final Vessel vessel
    ) {
        return new RandomExplorer<>(
            pathFinder.get(simulation).getAccessibleWaterCells(vessel.getCurrentCell()),
            simulation.random
        );
    }
}
