package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class ProtectedAreasOnlyFactoryTest
{


    @Test
    public void simpleFactoryTest() throws Exception
    {

        ProtectedAreasOnlyFactory factory = new ProtectedAreasOnlyFactory();
        final ProtectedAreasOnly first = factory.apply(mock(FishState.class));
        assertTrue(first!=null);

        final ProtectedAreasOnly second = factory.apply(mock(FishState.class));
        //they should be equal on account of being a singleton
        assertEquals(first,second);

    }

}