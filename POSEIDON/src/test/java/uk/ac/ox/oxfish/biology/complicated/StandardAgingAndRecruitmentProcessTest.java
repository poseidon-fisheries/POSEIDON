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

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/6/17.
 */
public class StandardAgingAndRecruitmentProcessTest {


    @Test
    public void oldFishDies() throws Exception {

        final Species species = mock(Species.class);
        when(species.getNumberOfBins()).thenReturn(3);
        final double[] male = {10, 20, 30};
        final double[] female = {100, 200, 300};
        final StructuredAbundance abundance = new StructuredAbundance(male, female);
        final StandardAgingProcess process = new StandardAgingProcess(false);

        final AbundanceLocalBiology bio = mock(AbundanceLocalBiology.class);
        when(bio.getAbundance(species)).thenReturn(abundance);


        process.ageLocally(bio, species, null, true, 365);

        Assertions.assertArrayEquals(male, new double[]{0, 10, 20}, .0001);
        Assertions.assertArrayEquals(female, new double[]{0, 100, 200}, .0001);

    }


    @Test
    public void oldFishStays() throws Exception {

        final Species species = mock(Species.class);
        when(species.getNumberOfBins()).thenReturn(3);
        final double[] male = {10, 20, 30};
        final double[] female = {100, 200, 300};
        final StructuredAbundance abundance = new StructuredAbundance(male, female);

        final StandardAgingProcess process = new StandardAgingProcess(true);

        final AbundanceLocalBiology bio = mock(AbundanceLocalBiology.class);
        when(bio.getAbundance(species)).thenReturn(abundance);


        process.ageLocally(bio, species, null, true, 365);

        System.out.println(Arrays.toString(male));
        Assertions.assertArrayEquals(male, new double[]{0, 10, 50}, .0001);
        Assertions.assertArrayEquals(female, new double[]{0, 100, 500}, .0001);

    }
}
