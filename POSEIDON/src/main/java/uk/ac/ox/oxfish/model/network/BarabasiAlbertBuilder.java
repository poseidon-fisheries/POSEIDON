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
import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Builds a network using Barabasi-Albert algorithm from Jung
 * Created by carrknight on 7/1/15.
 */
public class BarabasiAlbertBuilder extends AbstractNetworkBuilder
{


    private boolean parallelFriendshipsAllowed = true;

    private int edgesPerVertex = 2;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DirectedGraph<Fisher, FriendshipEdge> apply(FishState state) {

        final List<Fisher> fishers = state.getFishers();
        final Iterator<Fisher> fisherIterator = fishers.iterator();

        Preconditions.checkArgument(edgesPerVertex < fishers.size());


        BarabasiAlbertGenerator<Fisher,FriendshipEdge> generator =
                new BarabasiAlbertGenerator<>(DirectedSparseGraph::new,
                                              fisherIterator::next,
                                              FriendshipEdge::new,
                                              edgesPerVertex,
                                              edgesPerVertex,
                                              state.getRandom().nextInt(),
                                              new HashSet<>());


        final DirectedGraph<Fisher, FriendshipEdge> toReturn = (DirectedGraph<Fisher, FriendshipEdge>) generator.create();
        generator.evolveGraph(fishers.size()-edgesPerVertex);

        assert toReturn.getVertices().size() == fishers.size();


        return toReturn;

    }

    public boolean isParallelFriendshipsAllowed() {
        return parallelFriendshipsAllowed;
    }

    public void setParallelFriendshipsAllowed(boolean parallelFriendshipsAllowed) {
        this.parallelFriendshipsAllowed = parallelFriendshipsAllowed;
    }

    public int getEdgesPerVertex() {
        return edgesPerVertex;
    }

    public void setEdgesPerVertex(int edgesPerVertex) {
        this.edgesPerVertex = edgesPerVertex;
    }

    /**
     * this is supposed to be called not so much when initializing the network but later on if any agent is created
     * while the model is running
     *  @param newAddition
     * @param currentNetwork
     * @param state
     */
    @Override
    public void addFisher(
            Fisher newAddition, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state)
    {

        Preconditions.checkArgument(!currentNetwork.containsVertex(newAddition), "trying to add to the network an agent" +
                "that already exists");

        MersenneTwisterFast random = state.getRandom();


        List<NetworkPredicate> predicates = super.computePredicates(state);


        int totalDegree = currentNetwork.getVertices().stream().mapToInt(fisher -> currentNetwork.degree(fisher)).sum();
        currentNetwork.addVertex(newAddition);

        //follows classic barabassi:
        largeloop:
        while(currentNetwork.degree(newAddition)<edgesPerVertex)
            for(Fisher fisher : currentNetwork.getVertices() )
            {
                if(fisher==newAddition)
                    continue;
                if(
                        random.nextBoolean(currentNetwork.degree(fisher)/(double)totalDegree))
                {
                    boolean allowed = true;
                    for(NetworkPredicate predicate : predicates)
                        allowed = allowed && predicate.test(fisher,newAddition);
                    if(allowed) {
                        currentNetwork.addEdge(new FriendshipEdge(), newAddition, fisher);
                        if (currentNetwork.degree(newAddition) == edgesPerVertex)
                            break largeloop;
                    }
                }

            }


        assert currentNetwork.containsVertex(newAddition);

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
        Preconditions.checkArgument(currentNetwork.containsVertex(toRemove), "can't remove it if it doesn't exist");
        currentNetwork.removeVertex(toRemove);
        assert !currentNetwork.containsVertex(toRemove);

    }


}
