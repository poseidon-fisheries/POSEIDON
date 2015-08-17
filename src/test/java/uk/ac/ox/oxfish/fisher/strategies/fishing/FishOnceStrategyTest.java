package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishOnceFactory;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FishOnceStrategyTest
{

    @Test
    public void fishOnce() throws Exception {

        FisherEquipment equipment = mock(FisherEquipment.class);
        FishOnceStrategy strategy = new FishOnceFactory().apply(mock(FishState.class));


        when(equipment.getTotalPoundsCarried()).thenReturn(0d);
        //should be true: carrying nothing
        assertTrue(
                strategy.shouldFish(equipment,
                                    mock(FisherStatus.class),
                                    mock(FisherMemory.class),
                                    new MersenneTwisterFast(), mock(FishState.class))
        );

        when(equipment.getTotalPoundsCarried()).thenReturn(1d);
        //should be false: nothing is being carried
        assertFalse(
                strategy.shouldFish(equipment,
                                    mock(FisherStatus.class),
                                    mock(FisherMemory.class),
                                    new MersenneTwisterFast(), mock(FishState.class))
        );

    }
}