/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.network;

import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClubNetworkBuilderTest {

    @Test
    public void clubsOftwo() throws Exception {

        FishState state = mock(FishState.class);
        Fisher one = mock(Fisher.class);
        Fisher two = mock(Fisher.class);
        Fisher three = mock(Fisher.class);
        Fisher four = mock(Fisher.class);
        Fisher five = mock(Fisher.class);
        when(state.getFishers()).thenReturn(ObservableList.observableList(one, two, three, four, five));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        ClubNetworkBuilder builder = new ClubNetworkBuilder();
        builder.setClubSize(new FixedDoubleParameter(3));
        DirectedGraph<Fisher, FriendshipEdge> graph = builder.apply(state);

        assertEquals(graph.outDegree(one), 2);
        assertEquals(graph.outDegree(two), 2);
        assertEquals(graph.outDegree(three), 2);
        assertEquals(graph.outDegree(four), 1);
        assertEquals(graph.outDegree(five), 1);

        assertTrue(graph.isNeighbor(four, five));
        assertTrue(graph.isNeighbor(one, two));
        assertTrue(graph.isNeighbor(three, two));
        assertTrue(graph.isNeighbor(three, one));
        assertTrue(graph.isPredecessor(three, one));
        assertTrue(graph.isSuccessor(three, one));

        assertTrue(!graph.isNeighbor(four, one));
        assertTrue(!graph.isNeighbor(four, two));
        assertTrue(!graph.isNeighbor(four, three));


    }

}