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

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MonoQuotaPriceGeneratorTest {


    @Test
    public void testQuotaPrice() throws Exception {


        Fisher fisher = mock(Fisher.class);
        when(fisher.getDailyData()).thenReturn(mock(FisherDailyTimeSeries.class));
        FishState model = mock(FishState.class);
        when(model.getDayOfTheYear()).thenReturn(364);
        when(model.getSpecies()).thenReturn(Arrays.asList(new Species("a"), new Species("b"),
                                                          new Species("c"), new Species("d")));
        MonoQuotaRegulation regulation = new MonoQuotaRegulation(100);
        when(fisher.getRegulation()).thenReturn(regulation);

        when(fisher.predictUnitProfit(3)).thenReturn(10d);


        MonoQuotaPriceGenerator gen = new MonoQuotaPriceGenerator(3, false);

        gen.start(model, fisher);

        when(fisher.probabilitySumDailyCatchesBelow(3, 100, 1)).thenReturn(.5);
        //.5*10
        assertEquals(5, gen.computeLambda(), .0001);

        //change quotas
        regulation.setQuotaRemaining(0, 200);
        when(fisher.probabilitySumDailyCatchesBelow(3, 200, 1)).thenReturn(0d);
        assertEquals(10, gen.computeLambda(), .0001);


        when(model.getDayOfTheYear()).thenReturn(363);
        when(fisher.probabilitySumDailyCatchesBelow(3, 200, 2)).thenReturn(.5d);

        assertEquals(5, gen.computeLambda(), .0001);


    }

    @Test
    public void countingDailyProfits() throws Exception {

        Fisher fisher = mock(Fisher.class);
        when(fisher.getDailyData()).thenReturn(mock(FisherDailyTimeSeries.class));
        FishState model = mock(FishState.class);
        when(model.getSpecies()).thenReturn(Collections.singletonList(new Species("a")));

        MonoQuotaRegulation regulation = new MonoQuotaRegulation(200);
        when(fisher.getRegulation()).thenReturn(regulation);

        when(fisher.probabilitySumDailyCatchesBelow(0, 200, 2)).thenReturn(.5); //50%
        when(fisher.predictUnitProfit(0)).thenReturn(10d);
        when(fisher.predictDailyProfits()).thenReturn(10d);
        when(model.getDayOfTheYear()).thenReturn(363);

        MonoQuotaPriceGenerator gen = new MonoQuotaPriceGenerator(0,true);
        gen.start(model,fisher);

        // .5 * (10+10*2) = 15
        assertEquals(15d,gen.computeLambda(),.001d );

    }
}