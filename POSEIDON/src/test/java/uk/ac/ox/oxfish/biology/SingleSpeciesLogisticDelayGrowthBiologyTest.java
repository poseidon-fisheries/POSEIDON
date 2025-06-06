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

package uk.ac.ox.oxfish.biology;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SingleSpeciesLogisticDelayGrowthBiologyTest {


    @Test
    public void nofishing() throws Exception {

        final Species aaa = new Species("aaa");
        final SingleSpecieLogisticDelayGrowthBiology biology = new SingleSpecieLogisticDelayGrowthBiology(
            aaa,
            1000,
            1032,
            3,
            2,
            0

        );

        //step it for 10 years
        for (int i = 0; i < 10; i++)
            biology.step(mock(FishState.class));

        Assertions.assertEquals(1020, biology.getBiomass(aaa), .0001);

        //another 10 years and you hit the max
        for (int i = 0; i < 10; i++)
            biology.step(mock(FishState.class));

        Assertions.assertEquals(1032, biology.getBiomass(aaa), .0001);


    }


    @Test
    public void suddenlyFishing() throws Exception {

        final Species aaa = new Species("aaa");
        final SingleSpecieLogisticDelayGrowthBiology biology = new SingleSpecieLogisticDelayGrowthBiology(
            aaa,
            1000,
            1032,
            3,
            3,
            3

        );
        Assertions.assertEquals(1000, biology.getBiomass(aaa), .0001);
        final GlobalBiology global = mock(GlobalBiology.class);
        when(global.getSize()).thenReturn(1);
        biology.reactToThisAmountOfBiomassBeingFished(new Catch(aaa, 200, global),
            null, global
        );
        Assertions.assertEquals(800, biology.getBiomass(aaa), .0001);


        //step it for 10 years
        for (int i = 0; i < 10; i++)
            biology.step(mock(FishState.class));

        Assertions.assertEquals(829.8954875892, biology.getBiomass(aaa), .001);


    }
}
