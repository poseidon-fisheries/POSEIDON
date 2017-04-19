package uk.ac.ox.oxfish.fisher.equipment;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.assertEquals;


public class HoldTest {


    //load small loads correctly


    @Test
        public void loadCorrectly() throws Exception
    {
        Hold hold = new Hold(100,2);
        Species first = new Species("lame");
        Species second = new Species("second");
        GlobalBiology bio = new GlobalBiology(first,second);

        hold.load(new Catch(second,50.0,bio));
        assertEquals(50, hold.getTotalWeightOfCatchInHold(), .001);

        hold.load(new Catch(first, 10.0, bio));
        assertEquals(60, hold.getTotalWeightOfCatchInHold(), .001);

        hold.load(new Catch(first, 1.0, bio));
        assertEquals(61, hold.getTotalWeightOfCatchInHold(), .001);

        assertEquals(hold.getPercentageFilled(),.61,.001);


    }

    @Test
    public void throwsOverboard() throws Exception
    {
        Hold hold = new Hold(100,2);
        Species first = new Species("lame");
        Species second = new Species("second");
        GlobalBiology bio = new GlobalBiology(first,second);

        hold.load(new Catch(first, 100.0, bio));
        assertEquals(100, hold.getTotalWeightOfCatchInHold(), .001);

        hold.load(new Catch(second, 300.0, bio));
        assertEquals(100, hold.getTotalWeightOfCatchInHold(), .001);

        //has thrown stuff overboard
        assertEquals(25, hold.getWeightOfCatchInHold(first), .001);
        assertEquals(75, hold.getWeightOfCatchInHold(second), .001);



    }

    @Test
    public void unloadsCorrectly()
    {
        Hold hold = new Hold(100,2);
        Species first = new Species("lame");
        Species second = new Species("second");
        GlobalBiology bio = new GlobalBiology(first,second);

        hold.load(new Catch(first, 25.0, bio));
        hold.load(new Catch(second, 35.0, bio));
        assertEquals(60, hold.getTotalWeightOfCatchInHold(), .001);

        Catch caught = hold.unload();
        //should have emptied the hold
        assertEquals(0, hold.getTotalWeightOfCatchInHold(), .001);
        assertEquals(25.0,caught.getPoundsCaught(first),.001);
        //should not fuck up if I fill the hold again
        hold.load(new Catch(first, 15.0, bio));
        assertEquals(25.0, caught.getPoundsCaught(first), .001);
        assertEquals(15, hold.getTotalWeightOfCatchInHold(), .001);



    }
}