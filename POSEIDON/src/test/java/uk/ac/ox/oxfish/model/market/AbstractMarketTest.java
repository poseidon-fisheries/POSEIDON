/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.market;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;


public class AbstractMarketTest {


    @Test
    public void dataCollected() throws Exception {


        FishState model = mock(FishState.class);
        Species test = new Species("papapapap");
        AbstractMarket market = new FixedPriceMarket(1.0);
        market.setSpecies(test);
        List<Steppable> steppables = new LinkedList<>();
        doAnswer(invocation -> {
            steppables.add((Steppable) invocation.getArguments()[0]);
            return mock(Stoppable.class);
        }).when(model).scheduleEveryDay(
            any(), any());
        doAnswer(invocation -> {
            steppables.add((Steppable) invocation.getArguments()[0]);
            return mock(Stoppable.class);
        }).when(model).schedulePerPolicy(
            any(), any(), any());


        market.start(model);
        Assertions.assertEquals(2, steppables.size());
        market.recordTrade(new TradeInfo(100, test, 100));
        market.recordTrade(new TradeInfo(10, test, 200));
        steppables.get(1).step(model);
        steppables.get(0).step(model);

        final DataColumn landings = market.getData().getColumn(AbstractMarket.LANDINGS_COLUMN_NAME);
        final DataColumn earnings = market.getData().getColumn(AbstractMarket.EARNINGS_COLUMN_NAME);

        Assertions.assertEquals(110, landings.get(0), .0001);
        Assertions.assertEquals(300, earnings.get(0), .0001);

        steppables.get(1).step(model);
        steppables.get(0).step(model);
        Assertions.assertEquals(0, landings.get(1), .0001);
        Assertions.assertEquals(0, earnings.get(1), .0001);
    }
}