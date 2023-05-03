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

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 12/2/16.
 */
public class LogisticClassifierTest {


    @Test
    public void simpleTest() throws Exception {


        FishState state = mock(FishState.class);
        Fisher fisher = mock(Fisher.class);

        Log.info("The logistic regression tested here is just xb=1*id-2*dayOfTheYear");

        LogisticClassifier classifier =
                new LogisticClassifier(
                        new Pair<>(
                                (tile, timeOfObservation, agent, model) -> agent.getID(), 1d
                        ),
                        new Pair<>(
                                (tile, timeOfObservation, agent, model) -> model.getDayOfTheYear(), -2d
                        ));

        //if xb=0 the probability is 1/2
        when(fisher.getID()).thenReturn(0);
        when(state.getDayOfTheYear()).thenReturn(0);
        assertEquals(classifier.getProbability(fisher, state.getHoursSinceStart(), state, null),.5,.0001 );
        //if xb=-1 the probability is lower
        when(fisher.getID()).thenReturn(1);
        when(state.getDayOfTheYear()).thenReturn(1);
        assertEquals(classifier.getProbability(fisher, state.getHoursSinceStart(), state, null),0.2689414,.0001 );
        //if xb=1 the probability is higher
        when(fisher.getID()).thenReturn(3);
        when(state.getDayOfTheYear()).thenReturn(1);
        assertEquals(classifier.getProbability(fisher, state.getHoursSinceStart(), state, null),0.73105,.0001 );


    }
}