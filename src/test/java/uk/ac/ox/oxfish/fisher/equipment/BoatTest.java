package uk.ac.ox.oxfish.fisher.equipment;

import junit.framework.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 4/18/15.
 */
public class BoatTest {


    @Test
    public void storesCorrectly() throws Exception {

        Boat slowBoat = new Boat(5.0);
        assertEquals(2,slowBoat.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(10),.001); // 10 kilometers in 2 hours
        assertEquals(2,slowBoat.totalTravelTimeAfterAddingThisSegment(10),.001); // 10 kilometers in 2 hours
        slowBoat.recordTravel(10);
        assertEquals(2, slowBoat.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(10), .001); // again
        assertEquals(4, slowBoat.totalTravelTimeAfterAddingThisSegment(10), .001); // 4 hours total
        assertEquals(2,slowBoat.getHoursTravelledToday(),.001);

        slowBoat.recordTravel(2.5); //this should be another half an hour
        assertEquals(2.5, slowBoat.getHoursTravelledToday(), .001);





    }
}