package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MaximumStepsStrategyTest {


    @Test
    public void maximumSteps() throws Exception
    {
        MaximumStepsStrategy steps =  new MaximumStepsStrategy(100);

        //fish as long as it is BOTH not full and not being out for too long
        Fisher fisher = mock(Fisher.class);
        when(fisher.getMaximumLoad()).thenReturn(100d);

        when(fisher.getStepsAtSea()).thenReturn(50);
        when(fisher.getPoundsCarried()).thenReturn(50d);

        //both are true
        assertTrue(steps.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class)));

        when(fisher.getStepsAtSea()).thenReturn(50);
        when(fisher.getPoundsCarried()).thenReturn(100d);
        //full, will be false
        assertFalse(steps.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class)));


        when(fisher.getStepsAtSea()).thenReturn(101);
        when(fisher.getPoundsCarried()).thenReturn(50d);
        //too late, will be false
        assertFalse(steps.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class)));


    }
}