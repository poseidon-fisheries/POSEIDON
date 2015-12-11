package uk.ac.ox.oxfish.model.network;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Returns an empty network
 * Created by carrknight on 7/1/15.
 */
public class EmptyNetworkBuilder implements NetworkBuilder
{

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DirectedGraph<Fisher, FriendshipEdge> apply(FishState state) {
        return new DirectedSparseGraph<>();
    }

    /**
     * ignored
     */
    @Override
    public void addFisher(
            Fisher newAddition, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state) {
        //ignored
        assert !currentNetwork.containsVertex(newAddition);
    }

    /**
     * ignored
     */
    @Override
    public void removeFisher(
            Fisher toRemove, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state) {
        assert !currentNetwork.containsVertex(toRemove);

    }
}
