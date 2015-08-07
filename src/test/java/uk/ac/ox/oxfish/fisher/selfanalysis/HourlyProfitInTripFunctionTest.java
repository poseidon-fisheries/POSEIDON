package uk.ac.ox.oxfish.fisher.selfanalysis;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripLogger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 8/7/15.
 */
public class HourlyProfitInTripFunctionTest {


    @Test
    public void correct() throws Exception {

        Fisher fisher = mock(Fisher.class);
        TripLogger logger = new TripLogger();
        logger.newTrip();
        logger.recordCosts(100);
        logger.recordEarnings(100);
        logger.finishTrip(10);

        logger.newTrip();
        logger.recordCosts(100);
        logger.recordEarnings(200);
        logger.finishTrip(10);
        when(fisher.getFinishedTrips()).thenReturn(logger.getFinishedTrips());
        when(fisher.getLastFinishedTrip()).thenReturn(logger.getLastFinishedTrip());

        HourlyProfitInTripFunction tripFunction = new HourlyProfitInTripFunction();
        Assert.assertEquals(tripFunction.computeCurrentFitness(fisher),10d,.001);
        Assert.assertEquals(tripFunction.computePreviousFitness(fisher),0d,.001);
    }
}