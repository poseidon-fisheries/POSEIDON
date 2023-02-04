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

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.*;

/**
 * Builds network where everyone has the same out-degree of edges
 * Created by carrknight on 7/1/15.
 */
public class EquidegreeBuilder extends AbstractNetworkBuilder{

    private DoubleParameter degree = new FixedDoubleParameter(2d);

    /**
     * when this is false then do not allow both A->B and B->A connections
     */
    private boolean allowMutualFriendships = true;


    /**
     * when this is set to true, you are trying to target outdegree equal to the "degree" variable;
     * Otherwise you are trying to target indegree equal to "degree"
     */
    private boolean equalOutDegree = true;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DirectedGraph<Fisher, FriendshipEdge> apply(FishState state) {

        DirectedGraph<Fisher,FriendshipEdge> toReturn = new DirectedSparseGraph<>();

        //get all the fishers
        final List<Fisher> fishers = state.getFishers();
        final int populationSize = fishers.size();
        if(populationSize <= 1)
            Preconditions.checkArgument(
                    false, "Cannot create social network with no fishers to connect");

        Log.trace("random before populating " + state.getRandom().nextDouble());
        for(Fisher fisher : fishers)
        {
            int degree = computeDegree(state.getRandom());
            if( populationSize <= degree) {

                degree = populationSize-1;
                Log.warn("The social network had to reduce the desired degree level to " + degree + " because the population size is too small");

            }
            List<Fisher> friends = new LinkedList<>();

            List<NetworkPredicate> predicates = super.computePredicates(state);
            List<Fisher> candidates = new LinkedList<>();
            for(Fisher candidate : fishers)
            {
                boolean allowed = candidate!=fisher;
                boolean notConnected = equalOutDegree ? toReturn.findEdge(candidate,fisher) == null :  toReturn.findEdge(fisher,candidate) == null;
                boolean mutualAllowed = allowMutualFriendships || notConnected;
                for (NetworkPredicate predicate : predicates)
                    allowed = allowed && predicate.test(fisher, candidate);
                if(allowed  && mutualAllowed)
                    candidates.add(candidate);
            }


            Collections.sort(candidates, Comparator.comparingInt(Fisher::getID));

            while(friends.size() < degree && friends.size() < candidates.size())
            {
                int randomConnection = state.getRandom().nextInt(candidates.size());
                final Fisher candidate = candidates.get(randomConnection);
                assert (candidate != fisher);

                if(!friends.contains(candidate))
                    friends.add(candidate);


            }

            if(friends.size()<degree && Log.DEBUG)
            {
                assert friends.size()==candidates.size();
                Log.debug(fisher + " couldn't have " + degree + "friends because the total number of valid candidates" +
                                  " were " + candidates.size() +
                                  ", and the total number of friends the fisher actually has is " + friends.size());
            }



            //now make them your friends!

            if(friends.size() > 0)
                addSetOfFriends(toReturn, fisher, friends);
            else //if you have no friends add yourself as an unconnected person
                toReturn.addVertex(fisher);

        }

        return toReturn;

    }

    private void addSetOfFriends(
            DirectedGraph<Fisher, FriendshipEdge> network, Fisher fisher, Collection<Fisher> newConnections) {
        for(Fisher friend : newConnections) {
        if(equalOutDegree)
            network.addEdge(new FriendshipEdge(), fisher, friend, EdgeType.DIRECTED);
        else
            network.addEdge(new FriendshipEdge(), friend, fisher, EdgeType.DIRECTED);
        }
    }


    private int computeDegree(MersenneTwisterFast random){
        return degree.apply(random).intValue();
    }

    public DoubleParameter getDegree() {
        return degree;
    }

    public void setDegree(DoubleParameter degree) {
        this.degree = degree;
    }

    /**
     * this is supposed to be called not so much when initializing the network but later on if any agent is created
     * while the model is running
     *
     * @param fisher
     * @param currentNetwork
     * @param state
     */
    @Override
    public void addFisher(
            Fisher fisher, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state) {
        Preconditions.checkArgument(!currentNetwork.containsVertex(fisher));

        currentNetwork.addVertex(fisher);
        ObservableList<Fisher> fishers = state.getFishers();
        List<NetworkPredicate> predicates = super.computePredicates(state);



        int degree = computeDegree(state.getRandom());
        Set<Fisher> friends = new HashSet<>(degree);
        ArrayList<Fisher> candidates = new ArrayList<>(fishers);
        candidates.remove(fisher); //ignore yourself!
        while(friends.size() < degree && candidates.size() > 0)
        {
            final Fisher candidate = candidates.get(state.getRandom().nextInt(candidates.size()));
            if(candidate != fisher)
            {
                boolean allowed = true;
                for(NetworkPredicate predicate : predicates)
                    allowed = allowed && predicate.test(fisher,candidate);
                if(allowed)
                    friends.add(candidate);
            }
            candidates.remove(candidate);
        }
        //now make them your friends!
        addSetOfFriends(currentNetwork, fisher, friends);
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
        currentNetwork.removeVertex(toRemove);
    }

    /**
     * Getter for property 'allowMutualFriendships'.
     *
     * @return Value for property 'allowMutualFriendships'.
     */
    public boolean isAllowMutualFriendships() {
        return allowMutualFriendships;
    }

    /**
     * Setter for property 'allowMutualFriendships'.
     *
     * @param allowMutualFriendships Value to set for property 'allowMutualFriendships'.
     */
    public void setAllowMutualFriendships(boolean allowMutualFriendships) {
        this.allowMutualFriendships = allowMutualFriendships;
    }

    /**
     * Getter for property 'equalOutDegree'.
     *
     * @return Value for property 'equalOutDegree'.
     */
    public boolean isEqualOutDegree() {
        return equalOutDegree;
    }

    /**
     * Setter for property 'equalOutDegree'.
     *
     * @param equalOutDegree Value to set for property 'equalOutDegree'.
     */
    public void setEqualOutDegree(boolean equalOutDegree) {
        this.equalOutDegree = equalOutDegree;
    }
}
