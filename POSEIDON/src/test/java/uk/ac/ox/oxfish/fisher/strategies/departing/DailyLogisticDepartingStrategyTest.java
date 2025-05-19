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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 12/2/16.
 */
public class DailyLogisticDepartingStrategyTest {


    @Test
    public void dailyLogistic() throws Exception {

        Log.info("tests that the logistic strategy propagates the classifier correctly");


        LogisticClassifier classifier = mock(LogisticClassifier.class);

        DailyLogisticDepartingStrategy strategy =
            new DailyLogisticDepartingStrategy(classifier);

        //true gets propagated
        when(classifier.test(any(), any(), any(), any())).thenReturn(true);
        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        when(fisher.getHoursAtPort()).thenReturn(0d);

        //always true
        Assertions.assertTrue(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));
        Assertions.assertTrue(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));
        Assertions.assertTrue(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));
        Assertions.assertTrue(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));

        //if the classifier fails once
        when(classifier.test(any(), any(), any(), any())).thenReturn(false);
        Assertions.assertFalse(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));
        //then it will return automatically false without asking the classifier
        when(classifier.test(any(), any(), any(), any())).thenReturn(true);
        Assertions.assertFalse(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));
        Assertions.assertEquals(1, strategy.getDaysToWait());


        //until enough time passes
        when(fisher.getHoursAtPort()).thenReturn(23d);
        Assertions.assertFalse(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));
        when(fisher.getHoursAtPort()).thenReturn(24d);
        Assertions.assertTrue(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));
        Assertions.assertEquals(0, strategy.getDaysToWait());


    }
}
