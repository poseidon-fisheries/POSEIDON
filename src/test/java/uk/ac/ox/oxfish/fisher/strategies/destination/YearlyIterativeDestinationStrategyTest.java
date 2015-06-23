package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.YearlyFisherDataSet;
import uk.ac.ox.oxfish.utility.maximization.HillClimbingMovement;

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
                delegate, map, new MersenneTwisterFast());

        //mock fisher enough to fool delegate
        Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(delegate.getFavoriteSpot());
        final Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(mock(SeaTile.class));
        when(fisher.getHomePort()).thenReturn(port);


        //cashflow is x+y
        doAnswer(invocation -> delegate.getFavoriteSpot().getGridX() + delegate.getFavoriteSpot().getGridY()).when(
                fisher).getLatestYearlyObservation(YearlyFisherDataSet.CASH_FLOW_COLUMN);



        //step the hill-climber
        ((HillClimbingMovement)hill.getAlgorithm()).setMaxStepSize(1);
        hill.chooseDestination(fisher,fisher.grabRandomizer(),fishState,new Arriving()); //feed it the fisher
        //give it 1000 years!
        for(int i=0; i<1000; i++)
            hill.step(fishState);

        //should be very high
        System.out.print(delegate.getFavoriteSpot());
        Assert.assertTrue(delegate.getFavoriteSpot().getGridY() > 95);
        Assert.assertTrue(delegate.getFavoriteSpot().getGridX() > 95);

    }
}