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

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripIterativeDestinationFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PerTripIterativeDestinationStrategyTest {


    @Test
    public void hillclimbsCorrectly() throws Exception {

        //best fitness is 100,100 worse is 0,0; the agent starts at 50,50, can it make it towards the top?

        final FishState fishState = RandomThenBackToPortDestinationStrategyTest.generateSimpleSquareMap(100);
        NauticalMap map = fishState.getMap();
        MersenneTwisterFast random = new MersenneTwisterFast();
        when(fishState.getRandom()).thenReturn(random);
        when(fishState.getMap()).thenReturn(map);
        final FavoriteDestinationStrategy delegate = new FavoriteDestinationStrategy(
            map.getSeaTile(50, 50));
        final PerTripIterativeDestinationStrategy hill = new PerTripIterativeDestinationFactory().apply(fishState);

        //mock fisher enough to fool delegate
        Fisher fisher = mock(Fisher.class);
        when(fisher.grabRandomizer()).thenReturn(random);
        when(fisher.getLocation()).thenReturn(delegate.getFavoriteSpot());
        when(fisher.isGoingToPort()).thenReturn(false);
        final Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(mock(SeaTile.class));
        when(fisher.getHomePort()).thenReturn(port);

        when(fisher.getLocation()).thenReturn(delegate.getFavoriteSpot());
        when(fisher.isGoingToPort()).thenReturn(false);
        when(fisher.getHomePort()).thenReturn(port);


        hill.start(fishState, fisher);
        hill.getAlgorithm().start(fishState, fisher);
        SeaTile favoriteSpot = null;
        for (int i = 0; i < 1000; i++) {
            TripRecord record = mock(TripRecord.class);
            when(record.isCompleted()).thenReturn(true);
            when(record.isCutShort()).thenReturn(true);
            favoriteSpot = hill.chooseDestination(fisher,
                random, fishState, new Moving()
            );
            when(record.getProfitPerHour(anyBoolean())).thenReturn((double) (favoriteSpot.getGridX() + favoriteSpot.getGridY()));
            when(fisher.getLastFinishedTrip()).thenReturn(record);
            hill.getAlgorithm().adapt(fisher, fishState, random);

        }

        System.out.println(favoriteSpot.getGridX() + " --- " + favoriteSpot.getGridY());
        Assert.assertTrue(favoriteSpot.getGridX() > 90);
        Assert.assertTrue(favoriteSpot.getGridY() > 90);

    }
}