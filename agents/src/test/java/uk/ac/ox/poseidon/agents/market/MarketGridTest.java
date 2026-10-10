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

package uk.ac.ox.poseidon.agents.market;

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.util.Arrays;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarketGridTest {

    private final Port portA = mock(Port.class);
    private final Port portB = mock(Port.class);
    private final Market marketA = market(portA);
    private final Market marketB = market(portB);

    static Market market(final Port port) {
        final Market market = mock(Market.class);
        when(market.getPort()).thenReturn(port);
        return market;
    }

    /** @return a grid with all {@code markets} on the same cell */
    static MarketGrid marketGrid(final Market... markets) {
        final Int2D cell = new Int2D(1, 2);
        final ModelGrid modelGrid = mock(ModelGrid.class);
        when(modelGrid.getGridWidth()).thenReturn(10);
        when(modelGrid.getGridHeight()).thenReturn(10);
        return new ImmutableMarketGrid(
            modelGrid,
            Arrays.stream(markets).collect(toMap(identity(), market -> cell))
        );
    }

    @Test
    void givesTheMarketOfEachPortWhenTwoPortsShareACell() {
        final MarketGrid marketGrid = marketGrid(marketA, marketB);

        assertThat(marketGrid.getMarket(portA)).isSameAs(marketA);
        assertThat(marketGrid.getMarket(portB)).isSameAs(marketB);
    }

    @Test
    void findsTheMarketOfAPortWithoutScanningEveryMarket() {
        final MarketGrid marketGrid = marketGrid(marketA, marketB);
        clearInvocations(marketA, marketB);

        marketGrid.getMarket(portA);

        verify(marketB, never()).getPort();
    }

    @Test
    void throwsWithoutAMarketForThePort() {
        final MarketGrid marketGrid = marketGrid(marketB);

        assertThatThrownBy(() -> marketGrid.getMarket(portA))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void throwsWithMoreThanOneMarketForThePort() {
        final MarketGrid marketGrid = marketGrid(marketA, market(portA));

        assertThatThrownBy(() -> marketGrid.getMarket(portA))
            .isInstanceOf(IllegalStateException.class);
    }
}
