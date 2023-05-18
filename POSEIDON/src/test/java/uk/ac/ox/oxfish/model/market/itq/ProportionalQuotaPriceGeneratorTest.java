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

package uk.ac.ox.oxfish.model.market.itq;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscarding;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;

import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class ProportionalQuotaPriceGeneratorTest {


    @Test
    public void oneToOneQuota() throws Exception {


        HashMap<Integer, ITQOrderBook> orderBooks = new HashMap<>();
        orderBooks.put(0, mock(ITQOrderBook.class));
        orderBooks.put(1, mock(ITQOrderBook.class));

        FishState state = mock(FishState.class);
        when(state.getDayOfTheYear()).thenReturn(364); //one day left!
        when(state.getSpecies()).thenReturn(Arrays.asList(new Species("a"), new Species("b")));


        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        when(fisher.getDepartingStrategy().
            predictedDaysLeftFishingThisYear(any(), any(), any())).thenReturn(365 - 364);

        when(fisher.isAllowedAtSea()).thenReturn(true);
        when(fisher.getDailyData()).thenReturn(mock(FisherDailyTimeSeries.class));
        when(fisher.predictDailyCatches(0)).thenReturn(100d);
        when(fisher.predictDailyCatches(1)).thenReturn(100d);
        when(fisher.getDiscardingStrategy()).thenReturn(new NoDiscarding());

        when(fisher.probabilitySumDailyCatchesBelow(0, 123, 1)).thenReturn(.5); //50% chance of needing it

        when(fisher.predictUnitProfit(0)).thenReturn(1d);
        when(fisher.predictUnitProfit(1)).thenReturn(2d);
        when(orderBooks.get(1).getLastClosingPrice()).thenReturn(.5d);

        // .5 * ( 1 + (2 -.5)) = 2
        ProportionalQuotaPriceGenerator quota = new ProportionalQuotaPriceGenerator(
            orderBooks,
            0,
            fisher1 -> 123d
        );
        quota.start(state, fisher);
        Assert.assertEquals(1.25d, quota.computeLambda(), .001d);


        //now set the proportion to 1:3 (specie 0 is a choke specie)
        when(fisher.predictDailyCatches(1)).thenReturn(300d);
        //this increases the value of this quota, all things constant
        // .5 * ( 1 + 3*(2 -.5)) = 2
        Assert.assertEquals(2.75d, quota.computeLambda(), .001d);


    }
}