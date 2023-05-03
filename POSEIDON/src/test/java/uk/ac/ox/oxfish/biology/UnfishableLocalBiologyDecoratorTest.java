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

import static org.mockito.Mockito.*;

public class UnfishableLocalBiologyDecoratorTest {


    @Test
    public void protects() throws Exception {


        LocalBiology decorated = mock(LocalBiology.class);
        UnfishableLocalBiologyDecorator decorator = new UnfishableLocalBiologyDecorator(
                1,
                decorated
        );

        FishState state = mock(FishState.class);
        when(state.getYear()).thenReturn(0);
        decorator.start(state);

        decorator.reactToThisAmountOfBiomassBeingFished(null,null,null);
        verify(decorated,never()).reactToThisAmountOfBiomassBeingFished(any(),any(),any());


        when(state.getYear()).thenReturn(1);
        decorator.reactToThisAmountOfBiomassBeingFished(null,null,null);
        verify(decorated,times(1)).reactToThisAmountOfBiomassBeingFished(any(),any(),any());
    }
}