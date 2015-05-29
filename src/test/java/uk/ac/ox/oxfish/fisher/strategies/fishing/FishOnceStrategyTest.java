package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FishOnceStrategyTest
{

    @Test
    public void fishOnce() throws Exception {

        Fisher fisher = mock(Fisher.class);
        FishOnceStrategy strategy = FishOnceStrategy.FISH_ONCE_FACTORY.apply(mock(FishState.class));


        when(fisher.getPoundsCarried()).thenReturn(0d);
        //should be true: carrying nothing
        assertTrue(
                strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class))
        );

        when(fisher.getPoundsCarried()).thenReturn(1d);
        //should be false: nothing is being carried
        assertFalse(
                strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class))
        );

    }
}