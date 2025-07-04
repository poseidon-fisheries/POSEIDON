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

import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import static com.google.common.base.Preconditions.checkNotNull;

public class BiomassMarketGrid extends MarketGrid<Biomass, BiomassMarket> {

    private final PortGrid portGrid;

    BiomassMarketGrid(final PortGrid portGrid) {
        super(portGrid.getModelGrid());
        this.portGrid = portGrid;
    }

    public void addMarket(
        final BiomassMarket market,
        final Port port
    ) {
        final Int2D portLocation = portGrid.getLocation(port);
        checkNotNull(portLocation, "%s not found on port grid", port);
        getField().setObjectLocation(market, portLocation);
    }

    public void addMarket(
        final BiomassMarket market,
        final String portCode
    ) {
        final Port port = portGrid.getObject(portCode).orElseThrow(() ->
            new IllegalArgumentException(portCode + " not found on port grid")
        );
        addMarket(market, port);
    }

}
