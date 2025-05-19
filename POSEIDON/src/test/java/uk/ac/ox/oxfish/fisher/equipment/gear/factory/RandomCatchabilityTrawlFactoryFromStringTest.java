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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RandomCatchabilityTrawlFactoryFromStringTest {


    @Test
    public void simpleMap() throws Exception {

        RandomTrawlStringFactory factory = new RandomTrawlStringFactory();
        factory.setCatchabilityMap(" 0:1, 3:1 ");
        factory.setStandardDeviationMap(" 0:1 , 2  : 1");

        FishState state = mock(FishState.class);
        when(state.getSpecies()).thenReturn(Arrays.asList(
            new Species("0"),
            new Species("1"),
            new Species("2"),
            new Species("3")
        ));

        when(state.getRandom()).thenReturn(new MersenneTwisterFast());

        RandomCatchabilityTrawl gear = factory.apply(state);
        Assertions.assertEquals(gear.getCatchabilityMeanPerSpecie()[0], 1, .001);
        Assertions.assertEquals(gear.getCatchabilityMeanPerSpecie()[1], 0, .001);
        Assertions.assertEquals(gear.getCatchabilityMeanPerSpecie()[2], 0, .001);
        Assertions.assertEquals(gear.getCatchabilityMeanPerSpecie()[3], 1, .001);

        Assertions.assertEquals(gear.getCatchabilityDeviationPerSpecie()[0], 1, .001);
        Assertions.assertEquals(gear.getCatchabilityDeviationPerSpecie()[1], 0, .001);
        Assertions.assertEquals(gear.getCatchabilityDeviationPerSpecie()[2], 1, .001);
        Assertions.assertEquals(gear.getCatchabilityDeviationPerSpecie()[3], 0, .001);


    }


    @Test
    public void doubleParameterSupport() throws Exception {
        RandomTrawlStringFactory factory = new RandomTrawlStringFactory();
        factory.setCatchabilityMap("0: uniform 1 2");
        factory.setStandardDeviationMap("  ");

        FishState state = mock(FishState.class);
        when(state.getSpecies()).thenReturn(Collections.singletonList(new Species("0")));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());

        RandomCatchabilityTrawl gear = factory.apply(state);
        Assertions.assertTrue(gear.getCatchabilityMeanPerSpecie()[0] >= 1);
        Assertions.assertTrue(gear.getCatchabilityMeanPerSpecie()[0] <= 2);
    }
}
