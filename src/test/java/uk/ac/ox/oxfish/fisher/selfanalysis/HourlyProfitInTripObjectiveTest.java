package uk.ac.ox.oxfish.fisher.selfanalysis;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripLogger;
import uk.ac.ox.oxfish.geography.ports.Port;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 8/7/15.
 */
public class HourlyProfitInTripObjectiveTest {


    @Test
    public void correct() throws Exception {

        Fisher fisher = mock(Fisher.class);
        TripLogger logger = new TripLogger();
        logger.setNumberOfSpecies(1);
        logger.newTrip(0);
        logger.recordCosts(100);
        logger.recordEarnings(0,1,100);
        logger.finishTrip(10, mock(Port.class));

        logger.newTrip(0);
        logger.recordCosts(100);
        logger.recordEarnings(0,1,200);
        logger.finishTrip(10,mock(Port.class) );
        when(fisher.getFinishedTrips()).thenReturn(logger.getFinishedTrips());
        when(fisher.getLastFinishedTrip()).thenReturn(logger.getLastFinishedTrip());

        HourlyProfitInTripObjective tripFunction = new HourlyProfitInTripObjective();
        Assert.assertEquals(tripFunction.computeCurrentFitness(fisher, fisher), 10d, .001);
    }
}