package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class AnarchyFactoryTest {
    @Test
    public void simpleFactoryTest() throws Exception
    {

        AnarchyFactory factory = new AnarchyFactory();
        final Anarchy first = factory.apply(mock(FishState.class));
        assertTrue(first!=null);

        final Anarchy second = factory.apply(mock(FishState.class));
        //they should be equal on account of being a singleton
        assertEquals(first,second);

    }
}
