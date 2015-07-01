package uk.ac.ox.oxfish.model.network;

import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.Graph;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BarabasiAlbertBuilderTest
{

    @Test
    public void barabasi() throws Exception
    {


        BarabasiAlbertBuilder builder = new BarabasiAlbertBuilder();
        builder.setEdgesPerVertex(123);
        builder.setEdgesPerVertex(3);

        FishState fake = mock(FishState.class);
        LinkedList<Fisher> fishers = new LinkedList<>();
        for(int i=0; i<123; i++)
            fishers.add(mock(Fisher.class));
        when(fake.getFishers()).thenReturn(fishers);

        when(fake.getRandom()).thenReturn(new MersenneTwisterFast());

        final Graph<Fisher, FriendshipEdge> graph = builder.apply(fake);

        assertEquals(123,graph.getVertexCount());
        //the first 3 elements generate no edge.
        assertEquals(120*3,graph.getEdgeCount());



    }
}