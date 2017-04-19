package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
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
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(0d);
        when(fisher.getMaximumHold()).thenReturn(100d);


        //it's empty, you should fish
        assertTrue(
                strategy.shouldFish(fisher,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class),
                                    mock(TripRecord.class))
        );

        //it's 25% full, you should fish
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(25d);
        assertTrue(
                strategy.shouldFish(fisher,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class),
                                    mock(TripRecord.class))        );

        //it's 50% full, you shouldn't fish
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(50d);
        assertFalse(
                strategy.shouldFish(fisher,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class),
                                    mock(TripRecord.class))        );

        //it's 75& full, you shouldn't fish
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(75d);
        assertFalse(
                strategy.shouldFish(fisher,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class),
                                    mock(TripRecord.class))        );

        //if I change the minimum percentage, you should fish again
        strategy.setMinimumPercentageFull(85);
        assertTrue(
                strategy.shouldFish(fisher,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class),
                                    mock(TripRecord.class))        );

        //the threshold is satisfied even if the pounds carried are very slightly less(.001%) than the correct
        //minimum
        strategy.setMinimumPercentageFull(100d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(99.999d);
        when(fisher.getMaximumHold()).thenReturn(100d);
    }
}