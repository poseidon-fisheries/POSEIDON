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

package uk.ac.ox.oxfish.model.network;

import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BarabasiAlbertBuilderTest {

    @Test
    public void barabasi() throws Exception {


        BarabasiAlbertBuilder builder = new BarabasiAlbertBuilder();
        builder.setEdgesPerVertex(123);
        builder.setEdgesPerVertex(3);

        FishState fake = mock(FishState.class);
        ArrayList<Fisher> fishers = new ArrayList<>();
        for (int i = 0; i < 123; i++)
            fishers.add(mock(Fisher.class));
        when(fake.getFishers()).thenReturn(ObservableList.observableList(fishers));

        when(fake.getRandom()).thenReturn(new MersenneTwisterFast());

        final Graph<Fisher, FriendshipEdge> graph = builder.apply(fake);

        assertEquals(123, graph.getVertexCount());
        //the first 3 elements generate no edge.
        assertEquals(120 * 3, graph.getEdgeCount());


    }


    @Test
    public void incremental() throws Exception {


        BarabasiAlbertBuilder builder = new BarabasiAlbertBuilder();
        builder.setEdgesPerVertex(123);
        builder.setEdgesPerVertex(3);

        FishState fake = mock(FishState.class);
        ArrayList<Fisher> fishers = new ArrayList<>();
        for (int i = 0; i < 123; i++)
            fishers.add(mock(Fisher.class));
        when(fake.getFishers()).thenReturn(ObservableList.observableList(fishers));

        when(fake.getRandom()).thenReturn(new MersenneTwisterFast());

        final DirectedGraph<Fisher, FriendshipEdge> graph = builder.apply(fake);


        assertEquals(120 * 3, graph.getEdgeCount());


        builder.addFisher(mock(Fisher.class), graph, fake);
        builder.addFisher(mock(Fisher.class), graph, fake);
        Fisher last = mock(Fisher.class);
        builder.addFisher(last, graph, fake);
        assertEquals(123 * 3, graph.getEdgeCount());

        builder.removeFisher(last, graph, fake);


    }
}