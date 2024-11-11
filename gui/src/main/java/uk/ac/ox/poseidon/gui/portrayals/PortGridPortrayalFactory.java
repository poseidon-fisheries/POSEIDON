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

package uk.ac.ox.poseidon.gui.portrayals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sim.portrayal.grid.SparseGridPortrayal2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class PortGridPortrayalFactory extends SimulationScopeFactory<SparseGridPortrayal2D> {

    private Factory<? extends PortGrid> portGrid;

    @Override
    protected SparseGridPortrayal2D newInstance(final Simulation simulation) {
        final SparseGridPortrayal2D sparseGridPortrayal2D = new SparseGridPortrayal2D();
        sparseGridPortrayal2D.setField(portGrid.get(simulation).getField());
        sparseGridPortrayal2D.setPortrayalForAll(
            SvgPortrayal.from(getClass().getResourceAsStream("/images/port.svg"))
        );
        return sparseGridPortrayal2D;
    }
}
