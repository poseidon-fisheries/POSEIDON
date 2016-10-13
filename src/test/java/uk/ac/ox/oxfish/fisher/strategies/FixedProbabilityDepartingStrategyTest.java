package uk.ac.ox.oxfish.fisher.strategies;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FixedProbabilityDepartingStrategyTest {


    @Test
    public void alwaysDeparts() throws Exception {

        FixedProbabilityDepartingStrategy always = new FixedProbabilityDepartingStrategy(1.0, false);



        for(int i=0; i<50;i++) {
            assertTrue(always.shouldFisherLeavePort(mock(Fisher.class),mock(FishState.class),new MersenneTwisterFast()));
        }

    }

    @Test
    public void neverDeparts() throws Exception {

        FixedProbabilityDepartingStrategy never = new FixedProbabilityDepartingStrategy(0, false);
        for(int i=0; i<50;i++)
            assertFalse(never.shouldFisherLeavePort(mock(Fisher.class),mock(FishState.class),new MersenneTwisterFast()));

    }


    @Test
    public void departsSometimes() throws Exception
    {
        FixedProbabilityDepartingStrategy sometimes = new FixedProbabilityDepartingStrategy(.5, false);

        int departures = 0;
        for(int i=0; i<50;i++)
            if(sometimes.shouldFisherLeavePort(mock(Fisher.class),mock(FishState.class),new MersenneTwisterFast()));
                departures++;
        assertTrue(departures < 50);
        assertTrue(departures > 0);


    }

    @Test
    public void checksOnlyOnceADay() throws Exception {

        //100% probability but you keep asking the same day, you will only get one yes
        FixedProbabilityDepartingStrategy daily = new FixedProbabilityDepartingStrategy(1, true);

        FishState model = mock(FishState.class);
        when(model.getDay()).thenReturn(1d);
        int departures = 0;
        for(int i=0; i<50;i++) {
            if(daily.shouldFisherLeavePort(mock(Fisher.class),model,new MersenneTwisterFast()))
                departures++;
        }
        assertTrue(departures == 1);
        when(model.getDay()).thenReturn(2d);
        assertTrue(daily.shouldFisherLeavePort(mock(Fisher.class),model,new MersenneTwisterFast()));
    }
}