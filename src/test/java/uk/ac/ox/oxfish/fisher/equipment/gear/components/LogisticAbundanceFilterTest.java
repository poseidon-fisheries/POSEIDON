package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.*;

public class LogisticAbundanceFilterTest {


    @Test
    public void equality() throws Exception {

        AbundanceFilter first = new LogisticAbundanceFilter(10,20,true);
        AbundanceFilter second = new LogisticAbundanceFilter(10,20,true);
        AbundanceFilter third = new LogisticAbundanceFilter(10,200,true);

        assertFalse(first == second);
        assertFalse(first == third);

        assertTrue(first.equals(second));
        assertFalse(first.equals(third));

    }

    /**
     * numbers come from the spreadsheet and therefore stock assessment.
     */
    @Test
    public void computesCorrectly() throws Exception {
        Species species = new Species("Longspine",new Meristics(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                0.111313, 17.826, -1.79, 1,
                                                                0, 168434124,
                                                                0.6, false));
        LogisticAbundanceFilter filter = new LogisticAbundanceFilter(23.5053,9.03702,false);
        double[][] selectivity = filter.getSelectivity(species);
        assertEquals(selectivity[FishStateUtilities.MALE][5],0.1720164347,.001);
        assertEquals(selectivity[FishStateUtilities.FEMALE][20],0.5556124037,.001);

    }

    @Test
    public void memoizationIsFaster() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast();
        Species species = new Species("Longspine",new Meristics(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                0.111313, 17.826, -1.79, 1,
                                                                0, 168434124,
                                                                0.6, false));

        int[] male = new int[81];
        int[] female = new int[81];
        for(int i=0; i<81; i++)
        {
            male[i] = random.nextInt(100000);
            female[i] = random.nextInt(100000);
        }


        long start = System.currentTimeMillis();
        LogisticAbundanceFilter filter = new LogisticAbundanceFilter(23.5053,9.03702,false);
        for(int times=0;times<1000; times++)
            filter.filter(male,female,species);
        long end = System.currentTimeMillis();
        long durationFirst = end-start;

        start = System.currentTimeMillis();
        filter = new LogisticAbundanceFilter(23.5053,9.03702,true);
        for(int times=0;times<1000; times++)
            filter.filter(male,female,species);
        end = System.currentTimeMillis();

        long durationSecond = end-start;

        Log.info("After running a 1000 times the logistic filter, i expect the memoization time: " + durationSecond + ", to" +
                         "be less than the non-memoization time: " + durationFirst);
        assertTrue(durationFirst>durationSecond);


    }
}