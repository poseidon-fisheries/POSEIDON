package uk.ac.ox.oxfish.model.network;

import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Creates a network and manages adding more vertices to the graph
 * Created by carrknight on 12/11/15.
 */
public interface NetworkBuilder extends AlgorithmFactory<DirectedGraph<Fisher,FriendshipEdge>>
{


    /**
     * this is supposed to be called not so much when initializing the network but later on if any agent is created
     * while the model is running
     */
    public void addFisher(Fisher newAddition, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state);

    /**
     * remove fisher from network. This is to be used while the model is running to clear any ties
     * @param toRemove fisher to remove
     * @param currentNetwork network to modify
     * @param state
     */
    public void removeFisher(Fisher toRemove, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state);

}
