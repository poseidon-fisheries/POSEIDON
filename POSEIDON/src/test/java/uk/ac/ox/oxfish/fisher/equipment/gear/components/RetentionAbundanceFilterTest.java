/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.logging.Logger;


public class RetentionAbundanceFilterTest {


    @Test
    public void equality() throws Exception {

        final AbundanceFilter first = new RetentionAbundanceFilter(true, 10, 10, 10, true);
        final AbundanceFilter second = new RetentionAbundanceFilter(true, 10, 10, 10, true);
        final AbundanceFilter third = new RetentionAbundanceFilter(true, 10, 10, 100, true);

        Assertions.assertNotSame(first, second);
        Assertions.assertNotSame(first, third);

        Assertions.assertEquals(first, second);
        Assertions.assertNotEquals(first, third);

    }

    /**
     * numbers come from the spreadsheet and therefore stock assessment.
     */
    @Test
    public void computesCorrectly() throws Exception {
        final Species species = new Species(
            "Longspine",
            new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 17.826, -1.79, 1,
                0, 168434124,
                0.6, false
            )
        );
        final RetentionAbundanceFilter filter = new RetentionAbundanceFilter(false, 21.8035, 1.7773, 0.992661, true);
        final double[][] probability = filter.getProbabilityMatrix(species);
        Assertions.assertEquals(probability[FishStateUtilities.MALE][5], 0.004970534, .0001);
        Assertions.assertEquals(probability[FishStateUtilities.FEMALE][20], 0.8571669724, .001);

    }


    @Test
    public void filtersCorrectly() throws Exception {
        final Species species = new Species(
            "Longspine",
            new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 17.826, -1.79, 1,
                0, 168434124,
                0.6, false
            )
        );
        final RetentionAbundanceFilter filter = new RetentionAbundanceFilter(false, 21.8035, 1.7773, 0.992661, true);

        final double[] male = new double[81];
        final double[] female = new double[81];
        male[20] = 100;
        final double[][] abundance = new double[2][];
        abundance[FishStateUtilities.MALE] = male;
        abundance[FishStateUtilities.FEMALE] = female;
        final double[][] filtered = filter.filter(species, abundance);
        Assertions.assertEquals(filtered[FishStateUtilities.MALE][20], 86, .001);
        Assertions.assertEquals(filtered[FishStateUtilities.MALE][0], 0, .001);
        Assertions.assertEquals(filtered[FishStateUtilities.FEMALE][20], 0, .001);


    }

    @Test
    public void memoizationIsFaster() throws Exception {
        final MersenneTwisterFast random = new MersenneTwisterFast();
        final Species species = new Species(
            "Longspine",
            new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 17.826, -1.79, 1,
                0, 168434124,
                0.6, false
            )
        );

        final double[] male = new double[81];
        final double[] female = new double[81];
        for (int i = 0; i < 81; i++) {
            male[i] = random.nextInt(100000);
            female[i] = random.nextInt(100000);
        }
        final double[][] abundance = new double[2][];
        abundance[FishStateUtilities.MALE] = male;
        abundance[FishStateUtilities.FEMALE] = female;

        long start = System.currentTimeMillis();
        RetentionAbundanceFilter filter = new RetentionAbundanceFilter(false, 21.8035, 1.7773, 0.992661, true);
        for (int times = 0; times < 1000; times++)
            filter.filter(species, abundance);
        long end = System.currentTimeMillis();
        final long durationFirst = end - start;

        start = System.currentTimeMillis();
        filter = new RetentionAbundanceFilter(true, 21.8035, 1.7773, 0.992661, true);
        for (int times = 0; times < 1000; times++)
            filter.filter(species, abundance);
        end = System.currentTimeMillis();

        final long durationSecond = end - start;

        Logger.getGlobal()
            .info("After running a 1000 times the logistic filter, I expect the memoization time: " + durationSecond + ", to " +
                "be less than the non-memoization time: " + durationFirst);
        Assertions.assertTrue(durationFirst > durationSecond);


    }

}
