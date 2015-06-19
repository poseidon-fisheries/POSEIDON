package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.maximization.HillClimbingMovement;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PerTripIterativeDestinationStrategyTest {


    @Test
    public void hillclimbsCorrectly() throws Exception {

        //best fitness is 100,100 worse is 0,0; the agent starts at 50,50, can it make it towards the top?

        final FishState fishState = RandomThenBackToPortDestinationStrategyTest.generateSimpleSquareMap(100);
        NauticalMap map = fishState.getMap();
        final FavoriteDestinationStrategy delegate = new FavoriteDestinationStrategy(
                map.getSeaTile(50, 50));
        final PerTripIterativeDestinationStrategy hill = new PerTripIterativeDestinationStrategy(
                delegate, new HillClimbingMovement(map,new MersenneTwisterFast()));

        //mock fisher enough to fool delegate
        Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(delegate.getFavoriteSpot());
        final Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(mock(SeaTile.class));
        when(fisher.getHomePort()).thenReturn(port);


        for(int i=0; i<1000; i++)
        {
            TripRecord record = mock(TripRecord.class);
            when(record.isCompleted()).thenReturn(true);
            when(record.isCutShort()).thenReturn(true);
            final SeaTile favoriteSpot = delegate.getFavoriteSpot();
            when(record.getProfitPerStep()).thenReturn((double) (favoriteSpot.getGridX() + favoriteSpot.getGridY()));
            hill.reactToFinishedTrip(record);

        }



    }
}