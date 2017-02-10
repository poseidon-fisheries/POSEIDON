package uk.ac.ox.oxfish.model.network;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
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
    private final NetworkBuilder networkPopulator;


    public SocialNetwork(
            NetworkBuilder networkPopulator)
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
        if(Log.TRACE)
            Log.trace(agent + " changed friends from " + friendBeingReplaced + " to " + newFriend);



        return newFriend;

    }

    public void removeRandomFriend(Fisher agent,
                                   boolean ignoreDirection,
                                   MersenneTwisterFast random)
    {
        List<Fisher> friends = ignoreDirection ?
                ImmutableList.copyOf(getAllNeighbors(agent)) :
                ImmutableList.copyOf(getDirectedNeighbors(agent));
        Preconditions.checkArgument(friends.size() > 0, " Fisher has no friend that can be removed");

        Fisher exFriend = friends.get(random.nextInt(friends.size()));
        Preconditions.checkArgument(network.findEdgeSet(agent,exFriend).size()<=1);
        Preconditions.checkArgument(network.findEdgeSet(exFriend,agent).size()<=1);
        FriendshipEdge edge = network.findEdge(agent, exFriend);
        if(ignoreDirection && edge == null)
            edge = network.findEdge(exFriend,agent);
        Preconditions.checkArgument(edge!=null);
        network.removeEdge(edge);

        friends = ignoreDirection ?
                ImmutableList.copyOf(getAllNeighbors(agent)) :
                ImmutableList.copyOf(getDirectedNeighbors(agent));
        Preconditions.checkArgument(!friends.contains(exFriend));

    }

    public void addRandomFriend(Fisher agent,
                                   List<Fisher> otherFishers,
                                   MersenneTwisterFast random)
    {

        ArrayList<Fisher> candidates = new ArrayList<Fisher>(otherFishers);
        candidates.remove(agent);
        candidates.removeAll(getAllNeighbors(agent));

        Preconditions.checkArgument(candidates.size() > 0, " No valid candidate to befriend!");

        Fisher newFriend = candidates.get(random.nextInt(candidates.size()));
        network.addEdge(new FriendshipEdge(),agent,newFriend);


    }


    public String toMatrixFile()
    {
        StringBuffer buffer = new StringBuffer();
        for(Fisher fisher: network.getVertices())
        {
            for(Fisher friend : getDirectedNeighbors(fisher))
                buffer.append(fisher.getID()).append(",").append(friend.getID()).append("\n");
        }
        return buffer.toString();
    }


    /**
     * remove fisher from network. This is to be used while the model is running to clear any ties
     * @param toRemove fisher to remove
     * @param state
     */
    public void removeFisher(
            Fisher toRemove,
            FishState state) {
        networkPopulator.removeFisher(toRemove, network, state);
    }

    /**
     * this is supposed to be called not so much when initializing the network but later on if any agent is created
     * while the model is running
     * @param newAddition
     * @param state
     */
    public void addFisher(
            Fisher newAddition,
            FishState state) {
        networkPopulator.addFisher(newAddition, network, state);
    }

    /**
     * Getter forthe JUNG object
     *
     * @return Value for property 'network'.
     */
    public DirectedGraph<Fisher, FriendshipEdge> getBackingnetwork() {
        return network;
    }
}
