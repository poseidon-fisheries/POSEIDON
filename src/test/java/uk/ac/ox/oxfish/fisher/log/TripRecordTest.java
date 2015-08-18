package uk.ac.ox.oxfish.fisher.log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;


public class TripRecordTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void records() throws Exception 
    {
        TripRecord record = new TripRecord(1);
        record.recordCosts(100);
        record.recordEarnings(0,1,200);
        record.completeTrip(10);
        assertEquals(record.getProfitPerHour(),10,.001d);
    }




    @Test
    public void cannotChangeCompletedStuff(){
        TripRecord record = new TripRecord(0);
        assertFalse(record.isCompleted());
        //should not allow earnings after it's complete
        record.completeTrip(10);
        assertTrue(record.isCompleted());
        exception.expect(IllegalStateException.class);
        record.recordCosts(100);


    }
}