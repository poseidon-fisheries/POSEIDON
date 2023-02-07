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

package uk.ac.ox.oxfish.biology;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ConstantHeterogeneousLocalBiologyTest {

    @Test
    public void testManySpeciesBiology() throws Exception {

        ConstantHeterogeneousLocalBiology bio = new ConstantHeterogeneousLocalBiology(100,200,300);
        final Species species = mock(Species.class);
        when(species.getIndex()).thenReturn(0);
        assertEquals(100, bio.getBiomass(species), .001);

        when(species.getIndex()).thenReturn(1);
        assertEquals(200, bio.getBiomass(species), .001);
        GlobalBiology biology = new GlobalBiology(mock(Species.class),
                                                  species);
        bio.reactToThisAmountOfBiomassBeingFished(new Catch(species, 4000000d,
                                                            biology), null,
                                                  biology);//can't kill it off
        assertEquals(200, bio.getBiomass(species), .001);

    }
}