package uk.ac.ox.oxfish.model.network;

import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SocialNetworkTest {


    @Test
    public void replaceFriends() throws Exception {


        //create a network with a fake populator that only connects fisher 0 with fisher 1
        SocialNetwork network = new SocialNetwork(new NetworkBuilder() {
            @Override
            public DirectedGraph<Fisher, FriendshipEdge> apply(FishState fishState) {
                DirectedGraph<Fisher,FriendshipEdge> graph = new DirectedSparseGraph<>();
                for(Fisher fisher : fishState.getFishers())
                    graph.addVertex(fisher);
                graph.addEdge(new FriendshipEdge(),fishState.getFishers().get(0),fishState.getFishers().get(1));
                return graph;
            }

            /**
             * this is supposed to be called not so much when initializing the network but later on if any agent is created
             * while the model is running
             *
             * @param newAddition
             * @param currentNetwork
             * @param state
             */
            @Override
            public void addFisher(
                    Fisher newAddition, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state) {

            }

            /**
             * remove fisher from network. This is to be used while the model is running to clear any ties
             *
             * @param toRemove       fisher to remove
             * @param currentNetwork network to modify
             * @param state
             */
            @Override
            public void removeFisher(
                    Fisher toRemove, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state) {

            }

            /**
             * ignored
             */
            @Override
            public void addPredicate(NetworkPredicate predicate) {

            }
        });

        //the world will be made of 3 fishers
        ObservableList<Fisher> fishers = FXCollections.observableArrayList();
        Fisher zero = mock(Fisher.class);
        Fisher one = mock(Fisher.class);
        Fisher two = mock(Fisher.class);

        fishers.add(zero);
        fishers.add(one);
        fishers.add(two);
        FishState state = mock(FishState.class);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());
        when(state.getFishers()).thenReturn(fishers);

        network.populate(state);

        //check to make sure the population is correct
        Collection<Fisher> neighbors = network.getAllNeighbors(zero);
        assertEquals(neighbors.size(),1);
        neighbors.contains(one);
        //it's directed so the other way works as well
        neighbors = network.getDirectedNeighbors(zero);
        assertEquals(neighbors.size(),1);
        neighbors.contains(one);

        //1 instead has a friend only if you count it indirectly
        neighbors = network.getAllNeighbors(one);
        assertEquals(neighbors.size(),1);
        neighbors.contains(zero);
        neighbors =network.getDirectedNeighbors(one);
        assertEquals(neighbors.size(),0);


        //if I force a replacement it must connect 0 with 2
        Fisher newFriend = network.replaceFriend(zero,one,false,new MersenneTwisterFast(),fishers);
        assertEquals(newFriend,two);
        neighbors = network.getDirectedNeighbors(zero);
        assertEquals(neighbors.size(),1);
        neighbors.contains(two);
        //if I do it again, it will revert back to 1
        newFriend = network.replaceFriend(zero,two,false,new MersenneTwisterFast(),fishers);
        assertEquals(newFriend,one);
        neighbors = network.getDirectedNeighbors(zero);
        assertEquals(neighbors.size(),1);
        neighbors.contains(one);

    }
}