package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import static org.junit.Assert.*;


public class FishingSeasonFactoryTest {


    @Test
    public void randomSeason() throws Exception
    {


        FishState state = new FishState(System.currentTimeMillis());
        FishingSeasonFactory factory = new FishingSeasonFactory();
        factory.setSeasonLength(new UniformDoubleParameter(50,150));
        factory.setRespectMPA(false);
        for(int i=0; i<100;i++)
        {
            final FishingSeason season = factory.apply(state);
            assertTrue(season.getDaysOpened()>=50);
            assertTrue(season.getDaysOpened()<=150);
            assertFalse(season.isRespectMPAs());
        }


    }
}