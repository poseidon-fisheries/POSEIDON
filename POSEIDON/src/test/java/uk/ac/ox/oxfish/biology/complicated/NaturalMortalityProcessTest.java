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
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;

import java.nio.file.Paths;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 3/2/16.
 */
public class NaturalMortalityProcessTest {


    @Test
    public void mortalityTest() throws Exception {


        double male[] = new double[]{10000, 10000, 10000};
        double female[] = new double[]{5000, 4000, 3000};
        Meristics meristics = mock(StockAssessmentCaliforniaMeristics.class);
        when(meristics.getNumberOfSubdivisions()).thenReturn(2);


        NaturalMortalityProcess mortality = new ExponentialMortalityProcess(.1, .2);
        mortality.cull(meristics, true, new StructuredAbundance(male, female), 365);
        //this numbers I obtained in R
        Assertions.assertEquals(male[0], 9048, .001);
        Assertions.assertEquals(male[1], 9048, .001);
        Assertions.assertEquals(male[2], 9048, .001);

        Assertions.assertEquals(female[0], 4093, 1);
        Assertions.assertEquals(female[1], 3275, 1);
        Assertions.assertEquals(female[2], 2456, 1);


    }


    @Test
    public void sablefishMortality() throws Exception {

        Species species = MultipleSpeciesAbundanceInitializer.
            generateSpeciesFromFolder(Paths.get(
                "inputs",
                "california",
                "biology",
                "Sablefish"
            ), "Sablefish");

        double[] male = new double[60];
        double[] female = new double[60];
        Arrays.fill(male, 10000);


        NaturalMortalityProcess process = new ExponentialMortalityProcess(
            (StockAssessmentCaliforniaMeristics) species.getMeristics());

        process.cull(species.getMeristics(), true, new StructuredAbundance(male, female), 365);

        for (int i = 0; i < male.length; i++)
            Assertions.assertEquals(male[i], 9370, .001); //always round down now
        System.out.println(Arrays.toString(male));

    }
}
