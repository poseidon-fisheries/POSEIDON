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

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.assertEquals;



public class FixedProportionFilterTest
{


    @Test
    public void filtersCorrectly() throws Exception {


        Species species = new Species("Longspine", new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                      0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                      0.111313, 17.826, -1.79, 1,
                                                                      0, 168434124,
                                                                      0.6, false));
        FixedProportionFilter filter = new FixedProportionFilter(.2, true);

        double[] male = new double[81];
        double[] female = new double[81];
        male[20] = 100;

        double[][] abundance = new double[2][];
        abundance[FishStateUtilities.MALE] = male;
        abundance[FishStateUtilities.FEMALE] = female;
        double[][] filtered = filter.filter(species,abundance);
        assertEquals(filtered[FishStateUtilities.MALE][20],20,.0001);
        assertEquals(filtered[FishStateUtilities.MALE][21],0,.001);

        abundance = new double[2][];
        abundance[FishStateUtilities.MALE] = male;
        male[20] = 100;
        abundance[FishStateUtilities.FEMALE] = female;
        filter = new FixedProportionFilter(1, true);
        filtered = filter.filter(species,abundance);
        assertEquals(filtered[FishStateUtilities.MALE][20],100,.001);
        assertEquals(filtered[FishStateUtilities.MALE][21],0,.001);
    }
}