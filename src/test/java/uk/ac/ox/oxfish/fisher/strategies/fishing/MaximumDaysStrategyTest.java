package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MaximumDaysStrategyTest {


    @Test
    public void maximumSteps() throws Exception
    {
        MaximumDaysStrategy steps =  new MaximumDaysStrategy(100);

        //fish as long as it is BOTH not full and not being out for too long
        FisherEquipment equipment = mock(FisherEquipment.class);
        when(equipment.getMaximumLoad()).thenReturn(100d);
        when(equipment.getTotalPoundsCarried()).thenReturn(50d);

        FisherStatus status = mock(FisherStatus.class);
        when(status.getHoursAtSea()).thenReturn(50*24d);

        //both are true
        assertTrue(steps.shouldFish(equipment,status,mock(FisherMemory.class) ,
                                    new MersenneTwisterFast(), mock(FishState.class)));

        when(status.getHoursAtSea()).thenReturn(50*24d);
        when(equipment.getTotalPoundsCarried()).thenReturn(100d);
        //full, will be false
        assertFalse(steps.shouldFish(equipment,status,mock(FisherMemory.class) ,
                                     new MersenneTwisterFast(), mock(FishState.class)));

        when(status.getHoursAtSea()).thenReturn(101*24d);
        when(equipment.getTotalPoundsCarried()).thenReturn(50d);
        //too late, will be false
        assertFalse(steps.shouldFish(equipment,status,mock(FisherMemory.class) ,
                                     new MersenneTwisterFast(), mock(FishState.class)));


    }
}