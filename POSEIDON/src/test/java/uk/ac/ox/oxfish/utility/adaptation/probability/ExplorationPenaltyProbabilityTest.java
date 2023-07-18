/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.utility.adaptation.probability;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;


public class ExplorationPenaltyProbabilityTest {


    @Test
    public void explorationPenalty() throws Exception {


        final ExplorationPenaltyProbability probability = new ExplorationPenaltyProbability(1d, 1d, .5, .1);

        assertEquals(probability.getExplorationProbability(), 1d, .001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

        probability.judgeExploration(0, 100);
        assertEquals(probability.getExplorationProbability(), 1d, .001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);


        probability.judgeExploration(100, 0);
        assertEquals(probability.getExplorationProbability(), .5d, .001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

        probability.judgeExploration(100, 0);
        assertEquals(probability.getExplorationProbability(), .25d, .001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

        probability.judgeExploration(100, 0);
        assertEquals(probability.getExplorationProbability(), .125d, .001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

        probability.judgeExploration(100, 0);
        assertEquals(probability.getExplorationProbability(), .1d, .001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

        probability.judgeExploration(0, 100);
        assertEquals(probability.getExplorationProbability(), .15d, .001d);
        assertEquals(probability.getImitationProbability(), 1d, .001d);

    }
}