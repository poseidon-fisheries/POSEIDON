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

package uk.ac.ox.poseidon.gui.portrayals;

import lombok.*;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class MarketGridPortrayalFactory extends SimulationScopeFactory<SparseGridPortrayal2D> {

    private Factory<? extends MarketGrid<?, ?>> marketGrid;

    @Override
    protected SparseGridPortrayal2D newInstance(final @NonNull Simulation simulation) {
        final SparseGridPortrayal2D sparseGridPortrayal2D = new SparseGridPortrayal2D();
        sparseGridPortrayal2D.setField(marketGrid.get(simulation).getField());
        sparseGridPortrayal2D.setPortrayalForAll(
            new OvalPortrayal2D()
        );
        return sparseGridPortrayal2D;
    }
}
