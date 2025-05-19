/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs;

import com.beust.jcommander.internal.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExogenousPercentagePermitAllocationTest {


    @Test
    public void correctlyOrdersAndReturnsRightNumberOfBoats() {

        //create 10 fishers
        List<Fisher> fishers = new LinkedList<>();
        for (int fisher = 0; fisher < 10; fisher++) {
            Fisher fake = mock(Fisher.class);
            when(fake.getID()).thenReturn(fisher);
            fishers.add(fake);
        }
        FishState state = mock(FishState.class);
        when(state.getYear()).thenReturn(0);

        //the right proportion wanted is 20%, 30% and then 46%
        //this should result in 2 fishers, 3 fishers, and then always 4 fishers
        //they should also be the ones with the lowest ID
        List<Double> effortWanted = Lists.newArrayList(.2d, .3d, .4d);
        ExogenousPercentagePermitAllocation allocation = new ExogenousPercentagePermitAllocation(effortWanted);

        when(state.getYear()).thenReturn(0);
        Assertions.assertEquals(allocation.computeWhichFishersAreAllowed(fishers, state).size(), 2);

        when(state.getYear()).thenReturn(1);
        Assertions.assertEquals(allocation.computeWhichFishersAreAllowed(fishers, state).size(), 3);

        when(state.getYear()).thenReturn(2);
        Assertions.assertEquals(allocation.computeWhichFishersAreAllowed(fishers, state).size(), 4);

        when(state.getYear()).thenReturn(100);
        Assertions.assertEquals(allocation.computeWhichFishersAreAllowed(fishers, state).size(), 4);


        final List<Fisher> lastReturn = allocation.computeWhichFishersAreAllowed(fishers, state);
        for (Fisher fisher : lastReturn) {
            Assertions.assertTrue(fisher.getID() <= 3);
        }
    }
}
