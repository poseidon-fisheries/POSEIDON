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

package uk.ac.ox.poseidon.agents.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OneMarketPerPortBiomassMarketGridFactory
    extends SimulationScopeFactory<BiomassMarketGrid> {

    private Factory<? extends PortGrid> portGrid;

    @Override
    protected BiomassMarketGrid newInstance(final Simulation simulation) {
        final PortGrid portGrid = this.portGrid.get(simulation);
        final BiomassMarketGrid marketGrid = new BiomassMarketGrid(portGrid.getModelGrid());
        portGrid.getPorts().forEach(port -> {
            final BiomassMarket market = new BiomassMarket(
                port.getCode(),
                Map.of(), // TODO: allow specification of initial prices
                simulation.getEventManager()
            );
            marketGrid.getField().setObjectLocation(
                market,
                portGrid.getLocation(port)
            );
        });
        return marketGrid;
    }
}
