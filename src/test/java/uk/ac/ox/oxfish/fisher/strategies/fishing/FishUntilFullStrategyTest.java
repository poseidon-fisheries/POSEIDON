package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
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

        FisherEquipment equipment = mock(FisherEquipment.class);
        when(equipment.getTotalPoundsCarried()).thenReturn(0d);
        when(equipment.getMaximumLoad()).thenReturn(100d);


        //it's empty, you should fish
        assertTrue(
                strategy.shouldFish(equipment,
                                    mock(FisherStatus.class),
                                    mock(FisherMemory.class) ,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class))
        );

        //it's 25% full, you should fish
        when(equipment.getTotalPoundsCarried()).thenReturn(25d);
        assertTrue(
                strategy.shouldFish(equipment,
                                    mock(FisherStatus.class),
                                    mock(FisherMemory.class) ,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class))        );

        //it's 50% full, you shouldn't fish
        when(equipment.getTotalPoundsCarried()).thenReturn(50d);
        assertFalse(
                strategy.shouldFish(equipment,
                                    mock(FisherStatus.class),
                                    mock(FisherMemory.class) ,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class))        );

        //it's 75& full, you shouldn't fish
        when(equipment.getTotalPoundsCarried()).thenReturn(75d);
        assertFalse(
                strategy.shouldFish(equipment,
                                    mock(FisherStatus.class),
                                    mock(FisherMemory.class) ,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class))        );

        //if I change the minimum percentage, you should fish again
        strategy.setMinimumPercentageFull(85);
        assertTrue(
                strategy.shouldFish(equipment,
                                    mock(FisherStatus.class),
                                    mock(FisherMemory.class) ,
                                    new MersenneTwisterFast(),
                                    mock(FishState.class))        );

        //the threshold is satisfied even if the pounds carried are very slightly less(.001%) than the correct
        //minimum
        strategy.setMinimumPercentageFull(100d);
        when(equipment.getTotalPoundsCarried()).thenReturn(99.999d);
        when(equipment.getMaximumLoad()).thenReturn(100d);
    }
}