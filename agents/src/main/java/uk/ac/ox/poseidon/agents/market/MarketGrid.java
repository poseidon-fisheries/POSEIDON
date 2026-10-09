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

import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.ObjectGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/** An {@link ObjectGrid} of {@link Market}s, keyed by their code. */
public class MarketGrid extends ObjectGrid<Market> {

    MarketGrid(final ModelGrid modelGrid) {
        super(modelGrid);
    }

    /** @return {@code market}'s code */
    @Override
    protected String getObjectId(final Market market) {
        return market.getCode();
    }

    /**
     * Finds a market by the port it belongs to, not by its cell: ports can share a cell.
     *
     * @param port the port whose market to find
     * @return the one market of {@code port}
     * @throws IllegalStateException if {@code port} does not have exactly one market
     */
    public Market getMarket(final Port port) {
        final List<Market> markets = stream()
            .filter(market -> market.getPort().equals(port))
            .toList();
        checkState(
            markets.size() == 1,
            "Expected one market at port %s, found %s.",
            port,
            markets.size()
        );
        return markets.getFirst();
    }

}
