package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.*;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.DailyFisherTimeSeries;

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
                delegate, 1,10);

        //mock fisher enough to fool delegate
        Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(delegate.getFavoriteSpot());
        final Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(mock(SeaTile.class));
        when(fisher.getHomePort()).thenReturn(port);



        DailyFisherTimeSeries data = mock(DailyFisherTimeSeries.class);
        when(data.numberOfObservations()).thenReturn(8000);
        when(fisher.getDailyData()).thenReturn(data);




        //cashflow is x+y
        doAnswer(invocation -> delegate.getFavoriteSpot().getGridX() + delegate.getFavoriteSpot().getGridY()).when(
                fisher).getBankBalance();



        //step the hill-climber

        hill.getAlgorithm().start(fishState, mock(Fisher.class));

        //give it 1000 years!
        for(int i=0; i<1000; i++)
        {
            double bankBalance = fisher.getBankBalance();
            hill.getAlgorithm().adapt(fisher, new MersenneTwisterFast());
            when(fisher.balanceXDaysAgo(360)).thenReturn(bankBalance);

        }
        //should be very high
        System.out.print(delegate.getFavoriteSpot());
        Assert.assertTrue(delegate.getFavoriteSpot().getGridY() > 95);
        Assert.assertTrue(delegate.getFavoriteSpot().getGridX() > 95);

    }
}