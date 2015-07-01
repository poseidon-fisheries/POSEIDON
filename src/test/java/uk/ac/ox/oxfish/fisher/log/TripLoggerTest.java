package uk.ac.ox.oxfish.fisher.log;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class TripLoggerTest
{


    @Test
    public void logsHistoryCorrectly() throws Exception {


        TripLogger logger = new TripLogger();
        logger.start(mock(FishState.class));

        assertNull(logger.getCurrentTrip());
        assertEquals(logger.getFinishedTrips().size(),0);
        //create a new trip, now there is a current trip, but it's not in the history
        logger.newTrip();
        assertNotNull(logger.getCurrentTrip());
        assertEquals(logger.getFinishedTrips().size(),0);

        logger.recordEarnings(100);
        logger.recordCosts(200);
        logger.finishTrip(10);
        //even though it's over, it is still there as current trip
        assertTrue(logger.getCurrentTrip().isCompleted());
        assertEquals(logger.getCurrentTrip().getProfitPerStep(),-10,.001);
        assertEquals(logger.getFinishedTrips().size(),1);


    }

    @Test
    public void notifiesCorrectly() throws Exception {

        TripListener receiver = mock(TripListener.class);
        TripLogger logger = new TripLogger();
        logger.addTripListener(receiver);
        logger.newTrip();
        TripRecord record = logger.getCurrentTrip();

        logger.finishTrip(1);
        verify(receiver).reactToFinishedTrip(record);



    }
}