/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.regs;

import com.beust.jcommander.internal.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MaxHoldSizeRandomAllocationPolicyTest {


    @Test
    public void twoFishersGetIn() {

        Fisher one = mock(Fisher.class);
        when(one.getMaximumHold()).thenReturn(100d);
        Fisher two = mock(Fisher.class);
        when(two.getMaximumHold()).thenReturn(100d);
        Fisher three = mock(Fisher.class);
        when(three.getMaximumHold()).thenReturn(100d);
        Fisher four = mock(Fisher.class);
        when(four.getMaximumHold()).thenReturn(100d);


        List<Fisher> fishers = Lists.newArrayList(one,two,three,four);


        MaxHoldSizeRandomAllocationPolicy policy = new MaxHoldSizeRandomAllocationPolicy(200);

        FishState state = mock(FishState.class);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());

        for(int i=0; i < 100; i++) {
            List<Fisher> allowed = policy.computeWhichFishersAreAllowed(fishers, state);
            assertEquals(allowed.size(),2);
        }


    }

    @Test
    public void bigOnesGetBypassed() {

        //basically even if the order is random, if a boat is not allowed in then it shouldn't block other boats from joining

        Fisher one = mock(Fisher.class);
        when(one.getMaximumHold()).thenReturn(9999999d);
        Fisher two = mock(Fisher.class);
        when(two.getMaximumHold()).thenReturn(100d);
        Fisher three = mock(Fisher.class);
        when(three.getMaximumHold()).thenReturn(100d);
        Fisher four = mock(Fisher.class);
        when(four.getMaximumHold()).thenReturn(100d);


        List<Fisher> fishers = Lists.newArrayList(one,two,three,four);


        MaxHoldSizeRandomAllocationPolicy policy = new MaxHoldSizeRandomAllocationPolicy(300);

        FishState state = mock(FishState.class);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());

        for(int i=0; i < 100; i++) {
            List<Fisher> allowed = policy.computeWhichFishersAreAllowed(fishers, state);
            assertEquals(allowed.size(),3);
            assertTrue(!allowed.contains(one));
        }


    }
}