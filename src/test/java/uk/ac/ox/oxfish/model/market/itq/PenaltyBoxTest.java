package uk.ac.ox.oxfish.model.market.itq;

import com.esotericsoftware.minlog.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class PenaltyBoxTest {


    @Test
    public void penaltyBoxTest() throws Exception {
        Log.info("Here I check that people stay the right amount of time in the penalty box");

        Fisher one  = mock(Fisher.class);
        Fisher two  = mock(Fisher.class);

        PenaltyBox box = new PenaltyBox(10);
        box.registerTrader(one);
        assertTrue(box.has(one));
        assertTrue(!box.has(two));

        //step it 5 times
        for(int i=0; i<5; i++)
            box.step(mock(FishState.class));
        assertTrue(box.has(one));
        assertTrue(!box.has(two));

        //add two to the list
        box.registerTrader(two);
        assertTrue(box.has(one));
        assertTrue(box.has(two));

        //step it 5 times
        for(int i=0; i<5; i++)
            box.step(mock(FishState.class));
        //one should be out, two should be in
        assertTrue(!box.has(one));
        assertTrue(box.has(two));
    }
}