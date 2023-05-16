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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * A container of a Jung single directed graph.
 * The main issue with this object is instantiation: fishers (and their strategies) need to have a link to this
 * in order to  contact their friends, on the other hand fishers need to exist before we can link them up through a network.
 * <p>
 * The current idea is to have the social network passed empty to Fishers constructors and then populated in a second step
 * Created by carrknight on 7/1/15.
 */
public class SocialNetwork {


    /**
     * the algorithm that takes the model object and creates a full network which is then stored here.
     */
    private final NetworkBuilder networkPopulator;
    private DirectedGraph<Fisher, FriendshipEdge> network;


    public SocialNetwork(
        final NetworkBuilder networkPopulator
    ) {
        this.networkPopulator = networkPopulator;
    }


    public void populate(final FishState state) {
        Preconditions.checkArgument(network == null, "already populated!");
        network = networkPopulator.apply(state);

    }

    /**
     * has it been populated?
     *
     * @return true if it has!
     */
    public boolean isPopulated() {
        return network != null;
    }

    /**
     * these are all other fishers connected to this agent
     *
     * @param agent
     * @return
     */
    public Collection<Fisher> getPredecessors(final Fisher agent) {
        return network.getPredecessors(agent);
    }

    /**
     * replace connection with a random new one
     *
     * @param agent               agent who will be connected to a new friend
     * @param friendBeingReplaced old friend whose connection will be removed
     * @param ignoreDirection     should I check if the connection exists ignoring directions?
     * @return new friend
     */
    public Fisher replaceFriend(
        final Fisher agent,
        final Fisher friendBeingReplaced,
        final boolean ignoreDirection,
        final MersenneTwisterFast random,
        final List<Fisher> fishers
    ) {
        FriendshipEdge edge = network.findEdge(agent, friendBeingReplaced);
        if (ignoreDirection && edge == null)
            edge = network.findEdge(friendBeingReplaced, agent);
        Preconditions.checkArgument(edge != null, "cannot remove a friendship that isn't here!");
        network.removeEdge(edge);


        Fisher newFriend = null;
        while (newFriend == null) {
            final Fisher candidate = fishers.get(random.nextInt(fishers.size()));
            //if you are not already friends
            if (candidate != friendBeingReplaced &&
                candidate != agent &&
                !network.isPredecessor(agent, candidate) &&
                (!ignoreDirection || !network.isSuccessor(agent, candidate)))
                newFriend = candidate;
        }

        network.addEdge(new FriendshipEdge(), agent, newFriend);

        //log the change, if anybody is listening
        Logger.getGlobal().fine(agent + " changed friends from " + friendBeingReplaced + " to " + newFriend);


        return newFriend;

    }

    public void removeRandomFriend(
        final Fisher agent,
        final boolean ignoreDirection,
        final boolean bothSides,
        final MersenneTwisterFast random
    ) {
        List<Fisher> friends = ignoreDirection ?
            ImmutableList.copyOf(getAllNeighbors(agent)) :
            ImmutableList.copyOf(getDirectedNeighbors(agent));
        Preconditions.checkArgument(friends.size() > 0, " Fisher has no friend that can be removed");

        final Fisher exFriend = friends.get(random.nextInt(friends.size()));
        Preconditions.checkArgument(network.findEdgeSet(agent, exFriend).size() <= 1);
        Preconditions.checkArgument(network.findEdgeSet(exFriend, agent).size() <= 1);
        FriendshipEdge edge = network.findEdge(agent, exFriend);
        if (ignoreDirection && edge == null)
            edge = network.findEdge(exFriend, agent);
        Preconditions.checkArgument(edge != null);
        network.removeEdge(edge);

        if (bothSides) {
            edge = network.findEdge(exFriend, agent);
            if (edge != null)
                network.removeEdge(edge);

        }

        friends = ignoreDirection ?
            ImmutableList.copyOf(getAllNeighbors(agent)) :
            ImmutableList.copyOf(getDirectedNeighbors(agent));
        Preconditions.checkArgument(!friends.contains(exFriend));

    }

    /**
     * return all neighbors of this agent ignoring the direction of the edges
     */
    public Collection<Fisher> getAllNeighbors(final Fisher agent) {
        return network.getNeighbors(agent);
    }

    /**
     * get all fishers this agent connects to (that is there is an edge from the agent to his neighbors)
     *
     * @param agent the agent
     * @return a collection of agents
     */
    public Collection<Fisher> getDirectedNeighbors(final Fisher agent) {
        return network.getSuccessors(agent);
    }

    public void addRandomFriend(
        final Fisher agent,
        final List<Fisher> otherFishers,
        final MersenneTwisterFast random
    ) {

        final ArrayList<Fisher> candidates = new ArrayList<Fisher>(otherFishers);
        candidates.remove(agent);
        candidates.removeAll(getAllNeighbors(agent));

        Preconditions.checkArgument(candidates.size() > 0, " No valid candidate to befriend!");

        final Fisher newFriend = candidates.get(random.nextInt(candidates.size()));
        network.addEdge(new FriendshipEdge(), agent, newFriend);

        assert getDirectedNeighbors(agent).contains(newFriend);


    }


    /**
     * create a link from a friend to this agent (basically start sharing information with this other agent if we care about direct neighbors)
     *
     * @param agent        person who is going to share information
     * @param otherFishers set of all other fishers
     * @param random       randomizer
     */
    public void addRandomConnection(
        final Fisher agent,
        final List<Fisher> otherFishers,
        final MersenneTwisterFast random
    ) {

        final ArrayList<Fisher> candidates = new ArrayList<Fisher>(otherFishers);
        candidates.remove(agent);
        candidates.removeAll(network.getPredecessors(agent));

        Preconditions.checkArgument(candidates.size() > 0, " No valid candidate to befriend!");

        final Fisher newFriend = candidates.get(random.nextInt(candidates.size()));
        Preconditions.checkArgument(network.findEdge(newFriend, agent) == null);
        network.addEdge(new FriendshipEdge(), newFriend, agent);

        assert getDirectedNeighbors(newFriend).contains(agent);

    }

    public void removeRandomConnection(
        final Fisher agent,
        final MersenneTwisterFast random
    ) {
        final List<Fisher> friends = ImmutableList.copyOf(network.getPredecessors(agent));
        Preconditions.checkArgument(friends.size() > 0, " Fisher has no friend that can be removed");
        final Fisher exFriend = friends.get(random.nextInt(friends.size()));
        Preconditions.checkArgument(getAllNeighbors(agent).contains(exFriend));
        Preconditions.checkArgument(getDirectedNeighbors(exFriend).contains(agent));

        network.removeEdge(network.findEdge(exFriend, agent));


    }


    public String toMatrixFile() {
        final StringBuffer buffer = new StringBuffer();
        for (final Fisher fisher : network.getVertices()) {
            for (final Fisher friend : getDirectedNeighbors(fisher))
                buffer.append(fisher.getID()).append(",").append(friend.getID()).append("\n");
        }
        return buffer.toString();
    }


    /**
     * remove fisher from network. This is to be used while the model is running to clear any ties
     *
     * @param toRemove fisher to remove
     * @param state
     */
    public void removeFisher(
        final Fisher toRemove,
        final FishState state
    ) {
        networkPopulator.removeFisher(toRemove, network, state);
    }

    /**
     * this is supposed to be called not so much when initializing the network but later on if any agent is created
     * while the model is running
     *
     * @param newAddition
     * @param state
     */
    public void addFisher(
        final Fisher newAddition,
        final FishState state
    ) {
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
