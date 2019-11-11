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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CashFlowLogisticDepartingStrategyTest
{


    @Test
    public void departingCorrectly() throws Exception {


        CashFlowLogisticDepartingStrategy  strategy = new CashFlowLogisticDepartingStrategy(1,20,0.9,100,30);

        //create randomizer
        FishState model = mock(FishState.class);
        MersenneTwisterFast random = new MersenneTwisterFast();
        when(model.getRandom()).thenReturn(random);


        //30 days ago you had 10$
        Fisher fisher = mock(Fisher.class);
        when(fisher.numberOfDailyObservations()).thenReturn(100);
        when(fisher.balanceXDaysAgo(30)).thenReturn(10d);
        //now you have 105$
        when(fisher.getBankBalance()).thenReturn(105d);
        //your cashflow ought to be 95,compared to a 100$ target it means your daily probability of departing ought to be approx 26%
        //see the xlsx example
        int hoursDeparted = 0;
        for(int day =0;day <10000; day++ )
        {
            strategy.step(model);
            for(int hour=0; hour<24;hour++)
            {
                boolean departing = strategy.shouldFisherLeavePort(fisher, model, new MersenneTwisterFast());
                if(departing) {
                    hoursDeparted++;
                }
            }
        }

        double departingRate = hoursDeparted/(10000*24d);

        System.out.println(departingRate);
        assertTrue(departingRate > .20);
        assertTrue(departingRate < .30);





    }
}