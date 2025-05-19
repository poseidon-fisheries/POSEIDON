/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.network;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.*;
import java.util.logging.Logger;

/**
 * Builds network where everyone has the same out-degree of edges
 * Created by carrknight on 7/1/15.
 */
public class EquidegreeBuilder extends AbstractNetworkBuilder {

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
    public DirectedGraph<Fisher, FriendshipEdge> apply(final FishState state) {

        final DirectedGraph<Fisher, FriendshipEdge> toReturn = new DirectedSparseGraph<>();

        //get all the fishers
        final List<Fisher> fishers = state.getFishers();
        final int populationSize = fishers.size();
        if (populationSize <= 1)
            Preconditions.checkArgument(
                false, "Cannot create social network with no fishers to connect");

        Logger.getGlobal().fine("random before populating " + state.getRandom().nextDouble());
        for (final Fisher fisher : fishers) {
            int degree = computeDegree(state.getRandom());
            if (populationSize <= degree) {

                degree = populationSize - 1;
                Logger.getGlobal()
                    .warning("The social network had to reduce the desired degree level to " + degree + " because the population size is too small");

            }
            final List<Fisher> friends = new LinkedList<>();

            final List<NetworkPredicate> predicates = super.computePredicates(state);
            final List<Fisher> candidates = new LinkedList<>();
            for (final Fisher candidate : fishers) {
                boolean allowed = candidate != fisher;
                final boolean notConnected = equalOutDegree ? toReturn.findEdge(
                    candidate,
                    fisher
                ) == null : toReturn.findEdge(fisher, candidate) == null;
                final boolean mutualAllowed = allowMutualFriendships || notConnected;
                for (final NetworkPredicate predicate : predicates)
                    allowed = allowed && predicate.test(fisher, candidate);
                if (allowed && mutualAllowed)
                    candidates.add(candidate);
            }


            Collections.sort(candidates, Comparator.comparingInt(Fisher::getID));

            while (friends.size() < degree && friends.size() < candidates.size()) {
                final int randomConnection = state.getRandom().nextInt(candidates.size());
                final Fisher candidate = candidates.get(randomConnection);
                assert (candidate != fisher);

                if (!friends.contains(candidate))
                    friends.add(candidate);


            }

            if (friends.size() < degree) {
                assert friends.size() == candidates.size();
                final int finalDegree = degree;
                Logger.getGlobal()
                    .fine(() -> fisher + " couldn't have " + finalDegree + "friends because the total number of valid candidates" +
                        " were " + candidates.size() +
                        ", and the total number of friends the fisher actually has is " + friends.size());
            }


            //now make them your friends!

            if (friends.size() > 0)
                addSetOfFriends(toReturn, fisher, friends);
            else //if you have no friends add yourself as an unconnected person
                toReturn.addVertex(fisher);

        }

        return toReturn;

    }

    private int computeDegree(final MersenneTwisterFast random) {
        return (int) degree.applyAsDouble(random);
    }

    private void addSetOfFriends(
        final DirectedGraph<Fisher, FriendshipEdge> network,
        final Fisher fisher,
        final Collection<Fisher> newConnections
    ) {
        for (final Fisher friend : newConnections) {
            if (equalOutDegree)
                network.addEdge(new FriendshipEdge(), fisher, friend, EdgeType.DIRECTED);
            else
                network.addEdge(new FriendshipEdge(), friend, fisher, EdgeType.DIRECTED);
        }
    }

    public DoubleParameter getDegree() {
        return degree;
    }

    public void setDegree(final DoubleParameter degree) {
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
        final Fisher fisher, final DirectedGraph<Fisher, FriendshipEdge> currentNetwork, final FishState state
    ) {
        Preconditions.checkArgument(!currentNetwork.containsVertex(fisher));

        currentNetwork.addVertex(fisher);
        final ObservableList<Fisher> fishers = state.getFishers();
        final List<NetworkPredicate> predicates = super.computePredicates(state);


        final int degree = computeDegree(state.getRandom());
        final Set<Fisher> friends = new HashSet<>(degree);
        final ArrayList<Fisher> candidates = new ArrayList<>(fishers);
        candidates.remove(fisher); //ignore yourself!
        while (friends.size() < degree && candidates.size() > 0) {
            final Fisher candidate = candidates.get(state.getRandom().nextInt(candidates.size()));
            if (candidate != fisher) {
                boolean allowed = true;
                for (final NetworkPredicate predicate : predicates)
                    allowed = allowed && predicate.test(fisher, candidate);
                if (allowed)
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
        final Fisher toRemove, final DirectedGraph<Fisher, FriendshipEdge> currentNetwork, final FishState state
    ) {
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
    public void setAllowMutualFriendships(final boolean allowMutualFriendships) {
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
    public void setEqualOutDegree(final boolean equalOutDegree) {
        this.equalOutDegree = equalOutDegree;
    }
}
