package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.TowLimitFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/21/17.
 */
public class TowLimitFishingStrategyTest {


    @Test
    public void towing() throws Exception {


        TowLimitFactory factory = new TowLimitFactory();
        factory.setTowLimits(new FixedDoubleParameter(100d));
        FishingStrategy strategy = factory.apply(mock(FishState.class));
        TripRecord record = mock(TripRecord.class);
        when(record.getEffort()).thenReturn(1);
        Fisher fisher = mock(Fisher.class);
        when(fisher.getMaximumHold()).thenReturn(100d);
        assertTrue(
                strategy.shouldFish(fisher, new MersenneTwisterFast(),
                                    mock(FishState.class), record)
        );
        when(record.getEffort()).thenReturn(100);
        assertTrue(
                strategy.shouldFish(fisher, new MersenneTwisterFast(),
                                    mock(FishState.class), record)
        );

        when(record.getEffort()).thenReturn(101);
        assertFalse(
                strategy.shouldFish(fisher, new MersenneTwisterFast(),
                                    mock(FishState.class), record)
        );
    }
}