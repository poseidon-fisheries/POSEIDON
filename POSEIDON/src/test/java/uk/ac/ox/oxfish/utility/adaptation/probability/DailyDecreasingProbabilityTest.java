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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;


public class DailyDecreasingProbabilityTest {


    @Test
    public void decreasesCorrectly() throws Exception {

        final DailyDecreasingProbability probability = new DailyDecreasingProbability(1, 1, .5, .1);
        Assertions.assertEquals(probability.getExplorationProbability(), 1, .0001);
        Assertions.assertEquals(probability.getImitationProbability(), 1, .0001);

        probability.step(mock(FishState.class));
        Assertions.assertEquals(probability.getExplorationProbability(), .5, .0001);
        Assertions.assertEquals(probability.getImitationProbability(), 1, .0001);

        probability.step(mock(FishState.class));
        Assertions.assertEquals(probability.getExplorationProbability(), .25, .0001);
        Assertions.assertEquals(probability.getImitationProbability(), 1, .0001);

        probability.step(mock(FishState.class));
        Assertions.assertEquals(probability.getExplorationProbability(), .125, .0001);
        Assertions.assertEquals(probability.getImitationProbability(), 1, .0001);

        //threshold is bounding now
        probability.step(mock(FishState.class));
        Assertions.assertEquals(probability.getExplorationProbability(), .1, .0001);
        Assertions.assertEquals(probability.getImitationProbability(), 1, .0001);
    }
}