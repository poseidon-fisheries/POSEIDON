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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

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

        Assertions.assertEquals(graph.outDegree(one), 2);
        Assertions.assertEquals(graph.outDegree(two), 2);
        Assertions.assertEquals(graph.outDegree(three), 2);
        Assertions.assertEquals(graph.outDegree(four), 1);
        Assertions.assertEquals(graph.outDegree(five), 1);

        Assertions.assertTrue(graph.isNeighbor(four, five));
        Assertions.assertTrue(graph.isNeighbor(one, two));
        Assertions.assertTrue(graph.isNeighbor(three, two));
        Assertions.assertTrue(graph.isNeighbor(three, one));
        Assertions.assertTrue(graph.isPredecessor(three, one));
        Assertions.assertTrue(graph.isSuccessor(three, one));

        Assertions.assertTrue(!graph.isNeighbor(four, one));
        Assertions.assertTrue(!graph.isNeighbor(four, two));
        Assertions.assertTrue(!graph.isNeighbor(four, three));


    }

}