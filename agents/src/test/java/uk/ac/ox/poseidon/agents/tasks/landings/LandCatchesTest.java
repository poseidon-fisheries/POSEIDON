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

package uk.ac.ox.poseidon.agents.tasks.landings;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.market.Market;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.market.Sale;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LandCatchesTest {

    private final Int2D homePortCell = new Int2D(1, 2);
    private final Port homePort = mock(Port.class);
    private final Port otherPort = mock(Port.class);
    private final Market homePortMarket = mock(Market.class);
    private final Market otherPortMarket = mock(Market.class);
    private final MarketGrid marketGrid = mock(MarketGrid.class);
    private final Vessel vessel = mock(Vessel.class);
    private final Trip trip = mock(Trip.class);
    private final LandCatches landCatches = new LandCatches(() -> Duration.ZERO);

    @BeforeEach
    void setUp() throws Exception {
        when(homePortMarket.getPort()).thenReturn(homePort);
        when(otherPortMarket.getPort()).thenReturn(otherPort);
        final Sale sale = mock(Sale.class);
        when(sale.summary()).thenReturn(Map.of(CurrencyUnit.EUR, Money.of(CurrencyUnit.EUR, 1)));
        when(homePortMarket.sell(any(), any(), any())).thenReturn(sale);
        when(otherPortMarket.sell(any(), any(), any())).thenReturn(sale);
        when(vessel.getHomePort()).thenReturn(homePort);
        when(vessel.getHomePortLocation()).thenReturn(homePortCell);
        when(vessel.getCell()).thenReturn(homePortCell);
        when(vessel.getMarketGrid()).thenReturn(marketGrid);
        when(vessel.getHold()).thenReturn(mock(Hold.class));
        when(vessel.getSchedule()).thenReturn(mock(TemporalSchedule.class));
        when(trip.getAccount()).thenReturn(mock(Account.class));
        setTaskObject(landCatches, vessel);
        final Field tripField = LandCatches.class.getSuperclass().getDeclaredField("trip");
        tripField.setAccessible(true);
        tripField.set(landCatches, trip);
    }

    @Test
    void sellsAtTheMarketOfItsHomePort() {
        when(marketGrid.getMarket(homePort)).thenReturn(homePortMarket);

        landCatches.complete();

        verify(homePortMarket).sell(eq(vessel), any(), any());
    }

    @Test
    void throwsWhenTheVesselIsNotAtItsHomePort() {
        when(vessel.getCell()).thenReturn(new Int2D(3, 4));
        when(marketGrid.getMarket(otherPort)).thenReturn(otherPortMarket);

        assertThatThrownBy(landCatches::complete).isInstanceOf(IllegalStateException.class);
        verify(otherPortMarket, never()).sell(any(), any(), any());
    }

    @Test
    void throwsWithoutAHomePort() {
        when(vessel.getHomePort()).thenReturn(null);

        assertThatThrownBy(landCatches::complete).isInstanceOf(IllegalStateException.class);
        verify(homePortMarket, never()).sell(any(), any(), any());
    }

    private static void setTaskObject(final Task<?> task, final Object object) throws Exception {
        final BehaviorTree<Object> behaviorTree = new BehaviorTree<>();
        behaviorTree.setObject(object);
        final Field treeField = Task.class.getDeclaredField("tree");
        treeField.setAccessible(true);
        treeField.set(task, behaviorTree);
    }
}
