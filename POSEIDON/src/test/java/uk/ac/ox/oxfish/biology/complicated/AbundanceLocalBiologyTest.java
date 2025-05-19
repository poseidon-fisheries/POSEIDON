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

package uk.ac.ox.oxfish.biology.complicated;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.utility.FishStateUtilities;


public class AbundanceLocalBiologyTest {

    final static public MeristicsInput longspineTestInput = new MeristicsInput(80,
        40,
        3,
        8.573,
        27.8282,
        0.108505,
        4.30E-06,
        3.352,
        0.111313,
        3,
        8.573,
        27.8282,
        0.108505,
        4.30E-06,
        3.352,
        0.111313,
        17.826,
        -1.79,
        1,
        0,
        168434124,
        0.6,
        false
    );


    @Test
    public void longspineTotalBiomass() throws Exception {


        Species longspine = new Species("Longspine", longspineTestInput);
        GlobalBiology biology = new GlobalBiology(longspine);

        AbundanceLocalBiology local = new AbundanceLocalBiology(biology);

        //can modify directly
        local.getAbundance(longspine).asMatrix()[FishStateUtilities.FEMALE][5] = 100;
        local.getAbundance(longspine).asMatrix()[FishStateUtilities.MALE][5] = 200;
        local.getAbundance(longspine).asMatrix()[FishStateUtilities.MALE][6] = 100;

        Assertions.assertEquals(local.getBiomass(longspine), 100 * 0.019880139 +
            200 * 0.019880139 +
            100 * 0.0300262838, .001);


    }


    @Test
    public void fishOut() throws Exception {


        Species longspine = new Species("Longspine", longspineTestInput);
        GlobalBiology biology = new GlobalBiology(longspine);

        AbundanceLocalBiology local = new AbundanceLocalBiology(biology);

        //can modify directly
        local.getAbundance(longspine).asMatrix()[FishStateUtilities.FEMALE][5] = 100;
        local.getAbundance(longspine).asMatrix()[FishStateUtilities.MALE][5] = 200;
        local.getAbundance(longspine).asMatrix()[FishStateUtilities.MALE][6] = 100;

        double[] maleCatches = new double[longspine.getNumberOfBins()];
        double[] femaleCatches = new double[longspine.getNumberOfBins()];
        maleCatches[6] = 50;
        local.reactToThisAmountOfBiomassBeingFished(new Catch(maleCatches, femaleCatches, longspine, biology),
            null, biology
        );

        Assertions.assertEquals(local.getAbundance(longspine).asMatrix()[FishStateUtilities.FEMALE][5], 100, .001);
        Assertions.assertEquals(local.getAbundance(longspine).asMatrix()[FishStateUtilities.FEMALE][6], 0, .001);
        Assertions.assertEquals(local.getAbundance(longspine).asMatrix()[FishStateUtilities.MALE][5], 200, .001);
        Assertions.assertEquals(local.getAbundance(longspine).asMatrix()[FishStateUtilities.MALE][6], 50, .001);

    }

}
