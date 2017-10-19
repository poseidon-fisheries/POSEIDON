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

package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;

import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 3/2/16.
 */
public class NaturalMortalityProcessTest {


    @Test
    public void mortalityTest() throws Exception {


        int male[] = new int[]{10000,10000,10000};
        int female[] = new int[]{5000,4000,3000};
        Meristics meristics = mock(StockAssessmentCaliforniaMeristics.class);
        when(meristics.getMortalityParameterMFemale()).thenReturn(.2);
        when(meristics.getMortalityParameterMMale()).thenReturn(.1);

        NaturalMortalityProcess mortality = new NaturalMortalityProcess();
        mortality.cull(male,female,meristics);
        //this numbers I obtained in R
        assertEquals(male[0],9048);
        assertEquals(male[1],9048);
        assertEquals(male[2],9048);

        assertEquals(female[0], 4093,1);
        assertEquals(female[1],3275,1);
        assertEquals(female[2],2456,1);


    }


    @Test
    public void sablefishMortality() throws Exception {

        Species species = MultipleSpeciesAbundanceInitializer.
                generateSpeciesFromFolder(Paths.get("inputs",
                                                    "california",
                                                    "biology",
                                                    "Sablefish"),"Sablefish");

        int[] male = new int[60];
        int[] female = new int[60];
        Arrays.fill(male,10000);


        NaturalMortalityProcess process = new NaturalMortalityProcess();

        process.cull(male,female,species.getMeristics());

        for(int i=0; i<male.length; i++)
            assertEquals(male[i],9370); //always round down now
        System.out.println(Arrays.toString(male));

    }
}