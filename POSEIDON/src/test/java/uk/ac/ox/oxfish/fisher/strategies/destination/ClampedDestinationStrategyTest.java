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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.beust.jcommander.internal.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 8/8/17.
 */
public class ClampedDestinationStrategyTest {


    @Test
    public void clampedCensorsAndWorks() throws Exception {

            SeaTile tile1 = mock(SeaTile.class);
            SeaTile tile2 = mock(SeaTile.class);
            SeaTile tile3 = mock(SeaTile.class);

            SeaTile portLocation = mock(SeaTile.class);
            Port port = new Port("lame", portLocation,
                                 mock(MarketMap.class), 0);
            Fisher fisher = mock(Fisher.class);
            when(fisher.getHomePort()).thenReturn(port);

            FishState state = mock(FishState.class);

            MapDiscretization discretization = mock(MapDiscretization.class);
            when(discretization.getNumberOfGroups()).thenReturn(3);
            when(discretization.getGroup(0)).thenReturn(Lists.newArrayList(tile1));
            when(discretization.getGroup(1)).thenReturn(Lists.newArrayList(tile2));
            when(discretization.getGroup(2)).thenReturn(Lists.newArrayList(tile3));

            //you'd prefer to go to tile 3 a lot, but unfortunately it is beyond your max distance
            NauticalMap map = mock(NauticalMap.class);
            when(map.distance(tile1, portLocation)).thenReturn(10d);
            when(map.distance(tile2, portLocation)).thenReturn(20d);
            when(map.distance(tile3, portLocation)).thenReturn(2000d);

            when(state.getMap()).thenReturn(map);
            when(state.getRandom()).thenReturn(new MersenneTwisterFast());

            FavoriteDestinationStrategy delegate = mock(FavoriteDestinationStrategy.class);
            ClampedDestinationStrategy destinationStrategy = new ClampedDestinationStrategy(
                    delegate,
                    discretization,
                    100,
                    new double[]{1, 2, 100000},
                    false,
                    false
            );
            destinationStrategy.start(state, fisher);

            for (int i = 0; i < 100; i++)
                destinationStrategy.reactToFinishedTrip(null, null );


            verify(delegate, never()).setFavoriteSpot(tile3);
            verify(delegate, atMost(45)).setFavoriteSpot(tile1);
            verify(delegate, atLeast(55)).setFavoriteSpot(tile2);



    }
}