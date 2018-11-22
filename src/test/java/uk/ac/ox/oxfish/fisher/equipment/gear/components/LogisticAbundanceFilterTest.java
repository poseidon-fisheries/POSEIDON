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

public class LogisticAbundanceFilterTest {


    @Test
    public void equality() throws Exception {

        AbundanceFilter first = new LogisticAbundanceFilter(20, 10, true, true, true);
        AbundanceFilter second = new LogisticAbundanceFilter(20, 10, true, true, true);
        AbundanceFilter third = new LogisticAbundanceFilter(200, 10, true, true, true);

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
        Species species = new Species("Longspine",new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                     0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                     0.111313, 17.826, -1.79, 1,
                                                                     0, 168434124,
                                                                     0.6, false)
        );
        LogisticAbundanceFilter filter = new LogisticAbundanceFilter(23.5053, 9.03702, false, true, true);
        double[][] selectivity = filter.getProbabilityMatrix(species);
        assertEquals(selectivity[FishStateUtilities.MALE][5],0.1720164347,.001);
        assertEquals(selectivity[FishStateUtilities.FEMALE][20],0.5556124037,.001);

    }





    @Test
    public void filtersCorrectly() throws Exception {
        Species species = new Species("Longspine",
                                      new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                         0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                         0.111313, 17.826, -1.79, 1,
                                                         0, 168434124,
                                                         0.6, false)
        );
        LogisticAbundanceFilter filter = new LogisticAbundanceFilter(23.5053, 9.03702, false, true, true);

        double[] male = new double[81];
        double[] female = new double[81];
        male[5] = 100;
        double[][] abundance = new double[2][];
        abundance[FishStateUtilities.MALE] = male;
        abundance[FishStateUtilities.FEMALE] = female;
        double[][] filtered = filter.filter(species,abundance );
        assertEquals(filtered[FishStateUtilities.MALE][5],17,.001);
        assertEquals(filtered[FishStateUtilities.MALE][0],0,.001);
        assertEquals(filtered[FishStateUtilities.FEMALE][5],0,.001);


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
        LogisticAbundanceFilter filter = new LogisticAbundanceFilter(23.5053, 9.03702, false, true, true);
        for(int times=0;times<1000; times++)
            filter.filter(species, abundance);
        long end = System.currentTimeMillis();
        long durationFirst = end-start;

        start = System.currentTimeMillis();
        filter = new LogisticAbundanceFilter(23.5053, 9.03702, true, true, true);
        for(int times=0;times<1000; times++)
            filter.filter(species,abundance );
        end = System.currentTimeMillis();

        long durationSecond = end-start;

        Log.info("After running a 1000 times the logistic filter, I expect the memoization time: " + durationSecond + ", to " +
                         "be less than the non-memoization time: " + durationFirst);
        assertTrue(durationFirst>durationSecond);


    }
}