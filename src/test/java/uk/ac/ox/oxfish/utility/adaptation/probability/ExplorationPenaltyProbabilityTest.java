package uk.ac.ox.oxfish.utility.adaptation.probability;

import org.junit.Test;

import static org.junit.Assert.*;


public class ExplorationPenaltyProbabilityTest {


    @Test
    public void explorationPenalty() throws Exception
    {


        ExplorationPenaltyProbability probability = new ExplorationPenaltyProbability(1d,1d,.5,.1);

        assertEquals(probability.getExplorationProbability(),1d,.001d);
        assertEquals(probability.getImitationProbability(),1d,.001d);

        probability.judgeExploration(0, 100);
        assertEquals(probability.getExplorationProbability(),1d,.001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);


        probability.judgeExploration(100, 0);
        assertEquals(probability.getExplorationProbability(),.5d,.001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

        probability.judgeExploration(100, 0);
        assertEquals(probability.getExplorationProbability(),.25d,.001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

        probability.judgeExploration(100, 0);
        assertEquals(probability.getExplorationProbability(),.125d,.001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

        probability.judgeExploration(100, 0);
        assertEquals(probability.getExplorationProbability(),.1d,.001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

        probability.judgeExploration(0, 100);
        assertEquals(probability.getExplorationProbability(),.15d,.001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

    }
}