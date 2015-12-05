package uk.ac.ox.oxfish.model.network;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;
import java.util.List;

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


    /**
     * replace connection with a random new one
     * @param agent agent who will be connected to a new friend
     * @param friendBeingReplaced old friend whose connection will be removed
     * @param ignoreDirection should I check if the connection exists ignoring directions?
     * @return new friend
     */
    public Fisher replaceFriend(Fisher agent,
                                Fisher friendBeingReplaced,
                                boolean ignoreDirection,
                                MersenneTwisterFast random,
                                List<Fisher> fishers)
    {
        FriendshipEdge edge = network.findEdge(agent, friendBeingReplaced);
        if(ignoreDirection && edge == null)
            edge = network.findEdge(friendBeingReplaced,agent);
        Preconditions.checkArgument(edge!=null, "cannot remove a friendship that isn't here!");
        network.removeEdge(edge);


        Fisher newFriend = null;
        while(newFriend == null)
        {
            Fisher candidate = fishers.get(random.nextInt(fishers.size()));
            //if you are not already friends
            if(     candidate != friendBeingReplaced &&
                    candidate != agent  &&
                    !network.isPredecessor(agent,candidate) &&
                    (!ignoreDirection || !network.isSuccessor(agent,candidate)))
                newFriend = candidate;
        }

        network.addEdge(new FriendshipEdge(),agent,newFriend);

        //log the change, if anybody is listening
        if(Log.INFO)
            Log.info(agent + " changed friends from " + friendBeingReplaced + " to " + newFriend);



        return newFriend;

    }


    public String toMatrixFile()
    {
        return GraphMatrixOperations.graphToSparseMatrix(network).toString();
    }



}
