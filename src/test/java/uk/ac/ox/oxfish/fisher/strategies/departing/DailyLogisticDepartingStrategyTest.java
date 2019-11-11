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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
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
        when(classifier.test(any(),any(),any(),any())).thenReturn(true);
        Fisher fisher = mock(Fisher.class,RETURNS_DEEP_STUBS);
        when(fisher.getHoursAtPort()).thenReturn(0d);

        //always true
        assertTrue(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));
        assertTrue(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));
        assertTrue(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));
        assertTrue(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));

        //if the classifier fails once
        when(classifier.test(any(),any(),any(),any())).thenReturn(false);
        assertFalse(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));
        //then it will return automatically false without asking the classifier
        when(classifier.test(any(),any(),any(),any())).thenReturn(true);
        assertFalse(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));
        assertEquals(1,strategy.getDaysToWait());


        //until enough time passes
        when(fisher.getHoursAtPort()).thenReturn(23d);
        assertFalse(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));
        when(fisher.getHoursAtPort()).thenReturn(24d);
        assertTrue(strategy.shouldFisherLeavePort(fisher,mock(FishState.class),new MersenneTwisterFast()));
        assertEquals(0,strategy.getDaysToWait());


    }
}