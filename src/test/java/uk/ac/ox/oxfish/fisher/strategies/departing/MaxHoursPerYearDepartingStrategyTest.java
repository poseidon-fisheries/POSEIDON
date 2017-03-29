package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 3/29/17.
 */
public class MaxHoursPerYearDepartingStrategyTest {


    @Test
    public void maxHoursOut() throws Exception {
        Fisher fisher = mock(Fisher.class);
        MaxHoursPerYearDepartingStrategy strategy = new MaxHoursPerYearDepartingStrategy(100);
        when(fisher.getYearlyCounterColumn(FisherYearlyTimeSeries.HOURS_OUT)).thenReturn(50d);

        assertTrue(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));

        when(fisher.getYearlyCounterColumn(FisherYearlyTimeSeries.HOURS_OUT)).thenReturn(150d);
        assertFalse(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));


    }
}