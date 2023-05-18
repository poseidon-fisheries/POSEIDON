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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
        assertEquals(gear.getCatchabilityMeanPerSpecie()[0], 1, .001);
        assertEquals(gear.getCatchabilityMeanPerSpecie()[1], 0, .001);
        assertEquals(gear.getCatchabilityMeanPerSpecie()[2], 0, .001);
        assertEquals(gear.getCatchabilityMeanPerSpecie()[3], 1, .001);

        assertEquals(gear.getCatchabilityDeviationPerSpecie()[0], 1, .001);
        assertEquals(gear.getCatchabilityDeviationPerSpecie()[1], 0, .001);
        assertEquals(gear.getCatchabilityDeviationPerSpecie()[2], 1, .001);
        assertEquals(gear.getCatchabilityDeviationPerSpecie()[3], 0, .001);


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
        assertTrue(gear.getCatchabilityMeanPerSpecie()[0] >= 1);
        assertTrue(gear.getCatchabilityMeanPerSpecie()[0] <= 2);
    }
}