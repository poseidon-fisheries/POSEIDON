package uk.ac.ox.oxfish.fisher.strategies;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FixedProbabilityDepartingStrategyTest {


    @Test
    public void alwaysDeparts() throws Exception {

        FixedProbabilityDepartingStrategy always = new FixedProbabilityDepartingStrategy(1.0);
        MersenneTwisterFast random = new MersenneTwisterFast();
        Fisher fisher = mock(Fisher.class); when(fisher.getRandom()).thenReturn(random);

        for(int i=0; i<50;i++)
            assertTrue(always.shouldFisherLeavePort(fisher, null));

    }

    @Test
    public void neverDeparts() throws Exception {

        FixedProbabilityDepartingStrategy never = new FixedProbabilityDepartingStrategy(0);
        MersenneTwisterFast random = new MersenneTwisterFast();
        Fisher fisher = mock(Fisher.class); when(fisher.getRandom()).thenReturn(random);

        for(int i=0; i<50;i++)
            assertFalse(never.shouldFisherLeavePort(fisher, null));

    }


    @Test
    public void departsSometimes() throws Exception
    {
        FixedProbabilityDepartingStrategy sometimes = new FixedProbabilityDepartingStrategy(.5);
        MersenneTwisterFast random = new MersenneTwisterFast();
        Fisher fisher = mock(Fisher.class); when(fisher.getRandom()).thenReturn(random);

        int departures = 0;
        for(int i=0; i<50;i++)
            if(sometimes.shouldFisherLeavePort(fisher, null))
                departures++;
        assertTrue(departures < 50);
        assertTrue(departures > 0);


    }
}