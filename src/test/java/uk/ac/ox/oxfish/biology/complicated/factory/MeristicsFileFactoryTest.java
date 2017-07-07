package uk.ac.ox.oxfish.biology.complicated.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 7/7/17.
 */
public class MeristicsFileFactoryTest {


    @Test
    public void meristics() throws Exception
    {
        MeristicsFileFactory factory = new MeristicsFileFactory();
        factory.setPathToMeristicFile(Paths.get("inputs","california",
                                                "biology","Sablefish", "meristics.yaml"));
        StockAssessmentCaliforniaMeristics meristics = factory.apply(mock(FishState.class));


        assertEquals(meristics.getMaturitySlope(),-0.13,.001d);

    }
}