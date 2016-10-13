package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MonthlyDepartingStrategyTest {

    @Test
    public void monthsMatter() throws Exception {


        //january and may
        MonthlyDepartingStrategy strategy = new MonthlyDepartingStrategy(0,4);

        //you can depart in january
        FishState state = mock(FishState.class);
        when(state.getDayOfTheYear()).thenReturn(1);
        assertTrue(
                strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));


        when(state.getDayOfTheYear()).thenReturn(20);
        assertTrue(
                strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));




        //cannot depart february
        when(state.getDayOfTheYear()).thenReturn(40);
        assertFalse(
                strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));



        //can depart May
        when(state.getDayOfTheYear()).thenReturn(140);
        assertTrue(
                strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));


    }
}