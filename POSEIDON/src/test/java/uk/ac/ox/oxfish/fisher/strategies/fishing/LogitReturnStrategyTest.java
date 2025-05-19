/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 4/19/17.
 */
public class LogitReturnStrategyTest {


    @Test
    public void shouldIReturn() throws Exception {

        //empty fisher
        Fisher fisher = mock(Fisher.class);
        when(fisher.getMaximumHold()).thenReturn(100d);


        //always wants to return!
        LogisticClassifier classifier = mock(LogisticClassifier.class);

        LogitReturnStrategy strategy =
            new LogitReturnStrategy(classifier);


        //true gets propagated
        when(classifier.test(any(), any(), any(), any())).thenReturn(true);

        //always true because having put no fishing effort overrides the logit
        TripRecord record = mock(TripRecord.class);
        when(record.getEffort()).thenReturn(0);
        Assertions.assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
            record
        ));
        Assertions.assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
            record
        ));
        Assertions.assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
            record
        ));
        Assertions.assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
            record
        ));

        //when effort is at least 1, shouldFish returns the opposite of the classifier
        when(record.getEffort()).thenReturn(1);
        when(classifier.test(any(), any(), any(), any())).thenReturn(true);
        Assertions.assertFalse(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
            record
        ));
        Assertions.assertFalse(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
            record
        ));
        when(classifier.test(any(), any(), any(), any())).thenReturn(false);
        Assertions.assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
            record
        ));
        Assertions.assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
            record
        ));

        //you should return if when the classifier says not to if you are full
        when(classifier.test(any(), any(), any(), any())).thenReturn(false);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(100d);
        Assertions.assertFalse(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
            record
        ));
    }
}
