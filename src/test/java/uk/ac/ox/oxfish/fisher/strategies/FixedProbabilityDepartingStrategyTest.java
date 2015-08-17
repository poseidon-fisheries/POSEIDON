package uk.ac.ox.oxfish.fisher.strategies;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FixedProbabilityDepartingStrategyTest {


    @Test
    public void alwaysDeparts() throws Exception {

        FixedProbabilityDepartingStrategy always = new FixedProbabilityDepartingStrategy(1.0);

        FisherStatus status = mock(FisherStatus.class);
        when(status.getRandom()).thenReturn(new MersenneTwisterFast());


        for(int i=0; i<50;i++) {
            assertTrue(always.shouldFisherLeavePort(mock(FisherEquipment.class) ,
                                                    status,
                                                    mock(FisherMemory.class),null));
        }

    }

    @Test
    public void neverDeparts() throws Exception {

        FixedProbabilityDepartingStrategy never = new FixedProbabilityDepartingStrategy(0);
        FisherStatus status = mock(FisherStatus.class);
        when(status.getRandom()).thenReturn(new MersenneTwisterFast());
        for(int i=0; i<50;i++)
            assertFalse(never.shouldFisherLeavePort(mock(FisherEquipment.class),
                                                    status,
                                                    mock(FisherMemory.class), null));

    }


    @Test
    public void departsSometimes() throws Exception
    {
        FixedProbabilityDepartingStrategy sometimes = new FixedProbabilityDepartingStrategy(.5);
        FisherStatus status = mock(FisherStatus.class);
        when(status.getRandom()).thenReturn(new MersenneTwisterFast());

        int departures = 0;
        for(int i=0; i<50;i++)
            if(sometimes.shouldFisherLeavePort(mock(FisherEquipment.class),
                                               status,
                                               mock(FisherMemory.class), null))
                departures++;
        assertTrue(departures < 50);
        assertTrue(departures > 0);


    }
}