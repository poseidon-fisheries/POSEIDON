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
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GetterLocalBiologyTest {


    @Test
    public void getter() throws Exception {

        final Species species = mock(Species.class);
        final GetterLocalBiology localBiology =
            new GetterLocalBiology(
                species,
                state -> 10d + state.getDay()
            );

        final FishState state = mock(FishState.class);
        localBiology.start(state);

        when(state.getDay()).thenReturn(0);
        assertEquals(localBiology.getBiomass(species), 10d, .001);
        when(state.getDay()).thenReturn(10);
        assertEquals(localBiology.getBiomass(species), 20d, .001);


    }
}