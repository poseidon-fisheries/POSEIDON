package uk.ac.ox.oxfish.fisher.log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.ox.oxfish.fisher.Port;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class TripRecordTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void records() throws Exception 
    {
        TripRecord record = new TripRecord(1);
        record.recordCosts(100);
        record.recordEarnings(0,1,200);
        record.completeTrip(10, mock(Port.class));
        assertEquals(record.getProfitPerHour(false),10,.001d);
    }


    @Test
    public void opportunityCosts() throws Exception {


        TripRecord record = new TripRecord(1);
        record.recordCosts(100);
        record.recordOpportunityCosts(50);
        record.recordEarnings(0,1,200);
        record.completeTrip(10,mock(Port.class) );
        assertEquals(record.getProfitPerHour(false),10,.001d);
        assertEquals(record.getProfitPerHour(true),5,.001d);
    }

    @Test
    public void cannotChangeCompletedStuff(){
        TripRecord record = new TripRecord(0);
        assertFalse(record.isCompleted());
        //should not allow earnings after it's complete
        record.completeTrip(10,mock(Port.class) );
        assertTrue(record.isCompleted());
        exception.expect(IllegalStateException.class);
        record.recordCosts(100);


    }

    @Test
    public void profitsAreCorrect() throws Exception {
        TripRecord record = new TripRecord(2);
        record.recordCosts(100);
        record.recordEarnings(0,1,200);
        record.recordEarnings(1,1,100);
        record.completeTrip(10, mock(Port.class));
        assertEquals(record.getTotalTripProfit(),200,.001);

    }
}