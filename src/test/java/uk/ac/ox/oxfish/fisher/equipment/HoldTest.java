package uk.ac.ox.oxfish.fisher.equipment;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class HoldTest {


    //load small loads correctly


    @Test
        public void loadCorrectly() throws Exception
    {
        Species first = new Species("lame");
        Species second = new Species("second");
        GlobalBiology bio = new GlobalBiology(first,second);
        Hold hold = new Hold(100,bio);


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

        Species first = new Species("lame");
        Species second = new Species("second");
        GlobalBiology bio = new GlobalBiology(first,second);
        Hold hold = new Hold(100, bio);


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
        Species first = new Species("lame");
        Species second = new Species("second");
        GlobalBiology bio = new GlobalBiology(first,second);

        Hold hold = new Hold(100,bio);


        hold.load(new Catch(first, 25.0, bio));
        hold.load(new Catch(second, 35.0, bio));
        assertEquals(60, hold.getTotalWeightOfCatchInHold(), .001);

        Catch caught = hold.unload();
        //should have emptied the hold
        assertEquals(0, hold.getTotalWeightOfCatchInHold(), .001);
        assertEquals(25.0, caught.getWeightCaught(first), .001);
        //should not fuck up if I fill the hold again
        hold.load(new Catch(first, 15.0, bio));
        assertEquals(25.0, caught.getWeightCaught(first), .001);
        assertEquals(15, hold.getTotalWeightOfCatchInHold(), .001);



    }

    @Test
    public void throwsOverboardAbundance() throws Exception {

        Species first = mock(Species.class);
        when(first.getIndex()).thenReturn(0);
        Species second = mock(Species.class);
        when(second.getIndex()).thenReturn(1);

        GlobalBiology bio = new GlobalBiology(first,second);

        when(first.getMaxAge()).thenReturn(2);
        when(second.getMaxAge()).thenReturn(1);

        when(first.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        10d,20d,30d
                )
        );
        when(first.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        50d,50d,50d
                )
        );


        when(second.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );
        when(second.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );

        Hold hold = new Hold(1000d,
                             bio);

        hold.load(
                new Catch(
                        new int[]{0,10,0},
                        new int[]{10,0,0},
                        first,
                        bio

                )
        );

        assertEquals(hold.getTotalWeightOfCatchInHold(),
                     10*10+10*50d,.001d);
        assertEquals(hold.getWeightOfCatchInHold(first),
                     600d,0001d);
        assertEquals(hold.getWeightOfCatchInHold(second),
                     0d,0001d);

        assertEquals(hold.getWeightOfBin(first,0),100d,.001d);
        assertEquals(hold.getWeightOfBin(first,1),500d,.001d);
        assertEquals(hold.getWeightOfBin(first,2),0d,.001d);
        //catch the other species, too
        hold.load(
                new Catch(
                        new int[]{0,2},
                        new int[]{0,0},
                        second,
                        bio

                )
        );
        assertEquals(hold.getTotalWeightOfCatchInHold(),
                     800,.001d);
        assertEquals(hold.getWeightOfCatchInHold(first),
                     600d,0001d);
        assertEquals(hold.getWeightOfCatchInHold(second),
                     200d,0001d);

        assertEquals(hold.getWeightOfBin(first,0),100d,.001d);
        assertEquals(hold.getWeightOfBin(first,1),500d,.001d);
        assertEquals(hold.getWeightOfBin(first,2),0d,.001d);

        //so far so good, now we overload and we should throw away 50% of each

        //catch the other species, too
        hold.load(
                new Catch(
                        new int[]{0,12},
                        new int[]{0,0},
                        second,
                        bio

                )
        );
        //you had 2000kg, you can only hold 1000kg
        //you had 600-1400 now you ought to have 300-700
        assertEquals(hold.getTotalWeightOfCatchInHold(),
                     1000,.001d);
        assertEquals(hold.getWeightOfCatchInHold(first),
                     300d,0001d);
        assertEquals(hold.getWeightOfCatchInHold(second),
                     700d,0001d);


        assertEquals(hold.getWeightOfBin(first,0),50d,.001d);
        assertEquals(hold.getWeightOfBin(first,1),250d,.001d);
        assertEquals(hold.getWeightOfBin(first,2),0d,.001d);
        assertEquals(hold.getWeightOfBin(second,0),0d,.001d);
        assertEquals(hold.getWeightOfBin(second,1),700d,.001d);

    }
}