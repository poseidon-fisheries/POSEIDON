package uk.ac.ox.oxfish.utility.adaptation.probability;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class DailyDecreasingProbabilityTest {


    @Test
    public void decreasesCorrectly() throws Exception {

        DailyDecreasingProbability probability = new DailyDecreasingProbability(1,1,.5,.1);
        assertEquals(probability.getExplorationProbability(),1,.0001);
        assertEquals(probability.getImitationProbability(),1,.0001);

        probability.step(mock(FishState.class));
        assertEquals(probability.getExplorationProbability(), .5, .0001);
        assertEquals(probability.getImitationProbability(), 1, .0001);

        probability.step(mock(FishState.class));
        assertEquals(probability.getExplorationProbability(), .25, .0001);
        assertEquals(probability.getImitationProbability(), 1, .0001);

        probability.step(mock(FishState.class));
        assertEquals(probability.getExplorationProbability(), .125, .0001);
        assertEquals(probability.getImitationProbability(), 1, .0001);

        //threshold is bounding now
        probability.step(mock(FishState.class));
        assertEquals(probability.getExplorationProbability(), .1, .0001);
        assertEquals(probability.getImitationProbability(), 1, .0001);
    }
}