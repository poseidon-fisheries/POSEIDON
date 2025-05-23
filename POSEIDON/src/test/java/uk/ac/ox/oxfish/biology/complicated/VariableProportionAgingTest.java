/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;

public class VariableProportionAgingTest {


    @Test
    public void variableProportionAging() {


        VariableProportionAging aging = new VariableProportionAging(new double[][]{{.2, 0}});
        Species species = new Species("test",
            new GrowthBinByList(1, new double[]{1, 2}, new double[]{1, 2}), false
        );
        GlobalBiology biology = new GlobalBiology(species);

        AbundanceLocalBiology bio = new AbundanceLocalBiology(biology);

        bio.getAbundance(species).asMatrix()[0][0] = 1000d;
        bio.getAbundance(species).asMatrix()[0][1] = 100d;


        aging.start(species);
        aging.ageLocally(bio, species, mock(FishState.class), false, 365);


        Assertions.assertEquals(bio.getAbundance(species).asMatrix()[0][0], 800d, .001);
        Assertions.assertEquals(bio.getAbundance(species).asMatrix()[0][1], 300d, .001);

        aging.ageLocally(bio, species, mock(FishState.class), false, 1);
        //0.000547945
        Assertions.assertEquals(bio.getAbundance(species).asMatrix()[0][0], 799.561644, .001);
        Assertions.assertEquals(bio.getAbundance(species).asMatrix()[0][1], 300.438356, .001);

    }
}
