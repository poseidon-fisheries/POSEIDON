/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;

import static org.mockito.Mockito.*;


public class YearlyIterativeDestinationStrategyTest {


    @Test
    public void hillclimbs() throws Exception {

        //starts at 50,50. the farther from the origin the better

        final FishState fishState = RandomThenBackToPortDestinationStrategyTest.generateSimpleSquareMap(100);
        NauticalMap map = fishState.getMap();
        final FavoriteDestinationStrategy delegate = new FavoriteDestinationStrategy(
            map.getSeaTile(50, 50));
        final YearlyIterativeDestinationStrategy hill = new YearlyIterativeDestinationStrategy(
            delegate, 1, 10);

        //mock fisher enough to fool delegate
        Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(delegate.getFavoriteSpot());
        final Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(mock(SeaTile.class));
        when(fisher.getHomePort()).thenReturn(port);


        FisherDailyTimeSeries data = mock(FisherDailyTimeSeries.class);
        when(data.numberOfObservations()).thenReturn(8000);
        when(fisher.getDailyData()).thenReturn(data);


        //cashflow is x+y
        doAnswer(invocation -> (double) (delegate.getFavoriteSpot().getGridX() + delegate.getFavoriteSpot()
            .getGridY())).when(
            fisher).getBankBalance();
        when(fisher.balanceXDaysAgo(360)).thenReturn(0d);


        //step the hill-climber

        hill.getAlgorithm().start(fishState, mock(Fisher.class));

        //give it 1000 years!
        for (int i = 0; i < 2000; i++) {
            double bankBalance = fisher.getBankBalance();
            hill.getAlgorithm().adapt(fisher, fishState, new MersenneTwisterFast());


        }
        //should be very high
        Assertions.assertTrue(delegate.getFavoriteSpot().getGridY() > 95);
        Assertions.assertTrue(delegate.getFavoriteSpot().getGridX() > 95);

    }
}
