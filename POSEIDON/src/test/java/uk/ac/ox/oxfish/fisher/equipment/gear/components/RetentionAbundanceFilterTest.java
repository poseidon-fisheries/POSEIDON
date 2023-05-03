/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.*;


public class RetentionAbundanceFilterTest {



    @Test
    public void equality() throws Exception {

        AbundanceFilter first = new RetentionAbundanceFilter(true, 10, 10, 10, true);
        AbundanceFilter second = new RetentionAbundanceFilter(true, 10, 10, 10, true);
        AbundanceFilter third = new RetentionAbundanceFilter(true, 10, 10, 100, true);

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
        Species species = new Species("Longspine", new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                      0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                      0.111313, 17.826, -1.79, 1,
                                                                      0, 168434124,
                                                                      0.6, false)
        );
        RetentionAbundanceFilter filter = new RetentionAbundanceFilter(false, 21.8035, 1.7773, 0.992661, true);
        double[][] probability = filter.getProbabilityMatrix(species);
        assertEquals(probability[FishStateUtilities.MALE][5], 0.004970534, .0001);
        assertEquals(probability[FishStateUtilities.FEMALE][20],0.8571669724,.001);

    }





    @Test
    public void filtersCorrectly() throws Exception {
        Species species = new Species("Longspine",new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                                         0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                                         0.111313, 17.826, -1.79, 1,
                                                                                         0, 168434124,
                                                                                         0.6, false)
        );
        RetentionAbundanceFilter filter = new RetentionAbundanceFilter(false, 21.8035, 1.7773, 0.992661, true);

        double[] male = new double[81];
        double[] female = new double[81];
        male[20] = 100;
        double[][] abundance = new double[2][];
        abundance[FishStateUtilities.MALE] = male;
        abundance[FishStateUtilities.FEMALE] = female;
        double[][] filtered = filter.filter(species, abundance);
        assertEquals(filtered[FishStateUtilities.MALE][20],86,.001);
        assertEquals(filtered[FishStateUtilities.MALE][0],0,.001);
        assertEquals(filtered[FishStateUtilities.FEMALE][20],0,.001);


    }
    @Test
    public void memoizationIsFaster() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast();
        Species species = new Species("Longspine",new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                                         0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                                         0.111313, 17.826, -1.79, 1,
                                                                                         0, 168434124,
                                                                                         0.6, false)
        );

        double[] male = new double[81];
        double[] female = new double[81];
        for(int i=0; i<81; i++)
        {
            male[i] = random.nextInt(100000);
            female[i] = random.nextInt(100000);
        }
        double[][] abundance = new double[2][];
        abundance[FishStateUtilities.MALE] = male;
        abundance[FishStateUtilities.FEMALE] = female;

        long start = System.currentTimeMillis();
        RetentionAbundanceFilter filter =new RetentionAbundanceFilter(false, 21.8035, 1.7773, 0.992661, true);
        for(int times=0;times<1000; times++)
            filter.filter(species,abundance );
        long end = System.currentTimeMillis();
        long durationFirst = end-start;

        start = System.currentTimeMillis();
        filter =new RetentionAbundanceFilter(true, 21.8035, 1.7773, 0.992661, true);
        for(int times=0;times<1000; times++)
            filter.filter(species, abundance);
        end = System.currentTimeMillis();

        long durationSecond = end-start;

        Log.info("After running a 1000 times the logistic filter, I expect the memoization time: " + durationSecond + ", to " +
                         "be less than the non-memoization time: " + durationFirst);
        assertTrue(durationFirst>durationSecond);


    }

}