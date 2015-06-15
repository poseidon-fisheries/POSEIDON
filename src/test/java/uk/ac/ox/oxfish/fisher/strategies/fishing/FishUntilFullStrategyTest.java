package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishUntilFullFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FishUntilFullStrategyTest {

    @Test
    public void factoryTest() throws Exception {

        final FishUntilFullFactory factory = new FishUntilFullFactory();
        factory.setMinimumPercentageFull(new FixedDoubleParameter(.5)); //needs to be 50% full
        final FishUntilFullStrategy strategy = factory.apply(mock(FishState.class));

        Fisher fisher = mock(Fisher.class);
        when(fisher.getPoundsCarried()).thenReturn(0d);
        when(fisher.getMaximumLoad()).thenReturn(100d);


        //it's empty, you should fish
        assertTrue(
                strategy.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class))
        );

        //it's 25% full, you should fish
        when(fisher.getPoundsCarried()).thenReturn(25d);
        assertTrue(
                strategy.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class))
        );

        //it's 50% full, you shouldn't fish
        when(fisher.getPoundsCarried()).thenReturn(50d);
        assertFalse(
                strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class))
        );

        //it's 75& full, you shouldn't fish
        when(fisher.getPoundsCarried()).thenReturn(75d);
        assertFalse(
                strategy.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class))
        );

        //if I change the minimum percentage, you should fish again
        strategy.setMinimumPercentageFull(85);
        assertTrue(
                strategy.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class))
        );
    }
}