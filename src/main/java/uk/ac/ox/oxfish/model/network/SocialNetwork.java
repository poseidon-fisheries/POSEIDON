package uk.ac.ox.oxfish.model.network;

import com.google.common.base.Preconditions;
import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;

/**
 * A container of a Jung single directed graph.
 * The main issue with this object is instantiation: fishers (and their strategies) need to have a link to this
 * in order to  contact their friends, on the other hand fishers need to exist before we can link them up through a network.
 *
 * The current idea is to have the social network passed empty to Fishers constructors and then populated in a second step
 * Created by carrknight on 7/1/15.
 */
public class SocialNetwork
{


    private DirectedGraph<Fisher,FriendshipEdge> network;


    /**
     * the algorithm that takes the model object and creates a full network which is then stored here.
     */
    private final AlgorithmFactory<DirectedGraph<Fisher,FriendshipEdge>> networkPopulator;


    public SocialNetwork(
            AlgorithmFactory<DirectedGraph<Fisher, FriendshipEdge>> networkPopulator)
    {
        this.networkPopulator = networkPopulator;
    }


    public void populate(FishState state)
    {
        Preconditions.checkArgument(network == null, "already populated!");
        network = networkPopulator.apply(state);

    }

    /**
     * has it been populated?
     * @return true if it has!
     */
    public boolean isPopulated()
    {
        return network != null;
    }


    /**
     * return all neighbors of this agent ignoring the direction of the edges
     */
    public Collection<Fisher> getAllNeighbors(Fisher agent)
    {
        return network.getNeighbors(agent);
    }

    /**
     * get all fishers this agent connects to (that is there is an edge from the agent to his neighbors)
     * @param agent the agent
     * @return a collection of agents
     */
    public Collection<Fisher> getDirectedNeighbors(Fisher agent)
    {
        return network.getSuccessors(agent);
    }



}
