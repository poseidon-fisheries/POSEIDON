package uk.ac.ox.oxfish.model.regs;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
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
        List<Double> effortWanted = Lists.newArrayList(.2d,.3d,.4d);
        ExogenousPercentagePermitAllocation allocation = new ExogenousPercentagePermitAllocation(effortWanted);

        when(state.getYear()).thenReturn(0);
        assertEquals(allocation.computeWhichFishersAreAllowed(fishers,state).size(),2);

        when(state.getYear()).thenReturn(1);
        assertEquals(allocation.computeWhichFishersAreAllowed(fishers,state).size(),3);

        when(state.getYear()).thenReturn(2);
        assertEquals(allocation.computeWhichFishersAreAllowed(fishers,state).size(),4);

        when(state.getYear()).thenReturn(100);
        assertEquals(allocation.computeWhichFishersAreAllowed(fishers,state).size(),4);


        final List<Fisher> lastReturn = allocation.computeWhichFishersAreAllowed(fishers, state);
        for (Fisher fisher : lastReturn) {
            assertTrue(fisher.getID()<=3);
        }
    }
}