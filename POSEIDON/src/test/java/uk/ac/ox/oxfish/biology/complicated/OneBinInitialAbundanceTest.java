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

import static org.junit.Assert.assertEquals;

public class OneBinInitialAbundanceTest {


    @Test
    public void initialAbundanceSpecific() throws Exception {

        OneBinInitialAbundance initialAbundance = new OneBinInitialAbundance(1, 100, 0);

        //let's do longline
        Species longspine = new Species("Longspine", AbundanceLocalBiologyTest.longspineTestInput);
        initialAbundance.initialize(longspine);

        double[][] abundance = initialAbundance.getInitialAbundance();
        assertEquals(abundance.length, 2); //male and female
        assertEquals(abundance[0].length, 81); //80 years old fish
        assertEquals(abundance[0][0], 0d, .0001);
        assertEquals(abundance[1][1], 0d, .0001);
        assertEquals(abundance[0][1], 100d, .0001);
        assertEquals(abundance[0][2], 0d, .0001);

    }

    @Test
    public void initialAbundanceAllCohorts() throws Exception {

        OneBinInitialAbundance initialAbundance = new OneBinInitialAbundance(1, 100, -1);

        //let's do longline
        Species longspine = new Species("Longspine", AbundanceLocalBiologyTest.longspineTestInput);
        initialAbundance.initialize(longspine);

        double[][] abundance = initialAbundance.getInitialAbundance();
        assertEquals(abundance.length, 2); //male and female
        assertEquals(abundance[0].length, 81); //80 years old fish
        assertEquals(abundance[0][0], 0d, .0001);
        assertEquals(abundance[1][1], 100d, .0001);
        assertEquals(abundance[0][1], 100d, .0001);
        assertEquals(abundance[0][2], 0d, .0001);

    }
}