package uk.ac.ox.oxfish.model.network;

import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import javafx.collections.FXCollections;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EquidegreeBuilderTest {


    @Test
    public void everybodyGetsConnected() throws Exception {

        FishState state = mock(FishState.class);
        Fisher one = mock(Fisher.class);
        Fisher two = mock(Fisher.class);
        Fisher three = mock(Fisher.class);
        when(state.getFishers()).thenReturn(FXCollections.observableList(Arrays.asList(one,two,three)));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        EquidegreeBuilder builder = new EquidegreeBuilder();
        builder.setDegree(2);
        DirectedGraph<Fisher, FriendshipEdge> graph = builder.apply(state);

        assertEquals(graph.outDegree(one),2);
        assertEquals(graph.outDegree(two),2);
        assertEquals(graph.outDegree(three),2);

        builder.setDegree(1);
        graph = builder.apply(state);
        assertEquals(graph.outDegree(one),1);
        assertEquals(graph.outDegree(two),1);
        assertEquals(graph.outDegree(three),1);


        Fisher fourth = mock(Fisher.class);
        builder.addFisher(fourth,graph,state);
        assertEquals(graph.outDegree(one),1);
        assertEquals(graph.outDegree(two),1);
        assertEquals(graph.outDegree(three),1);
        assertEquals(graph.outDegree(fourth),1);
        builder.removeFisher(one,graph,state);






    }


    /**
     * "one and three" and "four and one" are never allowed to be friends!
     * @throws Exception
     */
    @Test
    public void predicateTest() throws Exception {

        FishState state = mock(FishState.class);
        Fisher one = mock(Fisher.class);
        Fisher two = mock(Fisher.class);
        Fisher three = mock(Fisher.class);

        when(state.getFishers()).thenReturn(FXCollections.observableList(Arrays.asList(one,two,three)));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        EquidegreeBuilder builder = new EquidegreeBuilder();
        builder.addPredicate((from, to) -> !(from==one && to==three));
        builder.setDegree(1);
        //build many many graphs
        for(int i=0; i<100; i++) {
            DirectedGraph<Fisher, FriendshipEdge> graph = builder.apply(state);
            assertEquals(graph.outDegree(one),1);
            assertFalse(graph.isSuccessor(one,three));

            Fisher fourth = mock(Fisher.class);
            builder.addPredicate((from, to) -> !(from==fourth && to==one));
            builder.addFisher(fourth,graph,state);
            assertFalse(graph.isSuccessor(fourth,one));

        }





    }
}