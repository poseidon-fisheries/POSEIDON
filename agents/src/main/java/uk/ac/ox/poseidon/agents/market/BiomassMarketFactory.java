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
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BiomassMarketFactory extends SimulationScopeFactory<BiomassMarket> {

    private Factory<? extends BiomassMarketGrid> marketGrid;
    private Factory<? extends Port> port;
    private String marketCode;
    private Factory<? extends Map<Species, Price>> pricesPerSpecies;

    @Override
    protected BiomassMarket newInstance(final Simulation simulation) {
        checkNotNull(marketGrid, "marketGrid must not be null");
        checkNotNull(port, "port must not be null");
        checkNotNull(pricesPerSpecies, "pricesPerSpecies must not be null");
        final Port port = this.port.get(simulation);
        final String marketCode = this.marketCode != null ? this.marketCode : port.getCode();
        final BiomassMarketGrid marketGrid = this.marketGrid.get(simulation);
        final BiomassMarket biomassMarket = new BiomassMarket(
            marketCode,
            pricesPerSpecies.get(simulation),
            simulation.getEventManager()
        );
        marketGrid.addMarket(biomassMarket, port);
        return biomassMarket;
    }
}
