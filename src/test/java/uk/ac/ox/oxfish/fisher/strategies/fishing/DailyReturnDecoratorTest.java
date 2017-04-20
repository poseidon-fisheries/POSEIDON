package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import org.mockito.Mockito;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 4/20/17.
 */
public class DailyReturnDecoratorTest {


    @Test
    public void overrides() throws Exception {


        Fisher fisher = mock(Fisher.class);
        TripRecord record = mock(TripRecord.class);

        //the decorated return strategy would always want to come back
        FishingStrategy decorated = mock(FishingStrategy.class);
        when(decorated.shouldFish(any(),any(),any(),any())).thenReturn(false);
        DailyReturnDecorator decorator = new DailyReturnDecorator(decorated);


        //if you haven't fished, or haven't been out for more than 24 then you are going to fish anyway
        when(record.getEffort()).thenReturn(0);
        when(fisher.getHoursAtSea()).thenReturn(0d);
        when(fisher.getMaximumHold()).thenReturn(100d);
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class),record));
        //still no fishing:
        when(fisher.getHoursAtSea()).thenReturn(100d);
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class),record));
        //now both overrides are off, you should return home!
        when(record.getEffort()).thenReturn(20);
        assertFalse(decorator.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class),record));
        assertTrue(decorator.getLastCheck()<=0); //last check is reset
    }


    @Test
    public void trip() throws Exception {
        Fisher fisher = mock(Fisher.class);
        TripRecord record = mock(TripRecord.class);

        //the decorated return strategy would always want to stay and fish
        FishingStrategy decorated = mock(FishingStrategy.class);
        when(decorated.shouldFish(any(),any(),any(),any())).thenReturn(true);
        DailyReturnDecorator decorator = new DailyReturnDecorator(decorated);


        //you spent 2 days at sea and fished once, it should return true by asking the decorated
        when(record.getEffort()).thenReturn(1);
        when(fisher.getHoursAtSea()).thenReturn(48d);
        when(fisher.getMaximumHold()).thenReturn(100d);
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class),record));
        verify(decorated,times(1)).shouldFish(any(),any(),any(),any());

        //now if you keep asking, it shouldn't call anymore until 24 hours pass
        Mockito.reset(decorated);
        when(decorated.shouldFish(any(),any(),any(),any())).thenReturn(false); //now the decorated wants to go home!
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class),record));
        when(fisher.getHoursAtSea()).thenReturn(50d);
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class),record));
        when(fisher.getHoursAtSea()).thenReturn(54d);
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class),record));
        verify(decorated,times(0)).shouldFish(any(),any(),any(),any());
        assertEquals(decorator.getLastCheck(),48d,.0001d);

        //but if enough hours pass, you will check again, and then go home
        when(fisher.getHoursAtSea()).thenReturn(72d);
        assertFalse(decorator.shouldFish(fisher,new MersenneTwisterFast(),mock(FishState.class),record));
        verify(decorated,times(1)).shouldFish(any(),any(),any(),any());
        assertTrue(decorator.getLastCheck()<=0);





    }

}