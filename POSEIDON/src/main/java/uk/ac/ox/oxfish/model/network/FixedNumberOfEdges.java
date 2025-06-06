/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Collection;
import java.util.List;

public class FixedNumberOfEdges extends AbstractNetworkBuilder {


    private DoubleParameter edges = new FixedDoubleParameter(100d);

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
        final Fisher newAddition, final DirectedGraph<Fisher, FriendshipEdge> currentNetwork, final FishState state
    ) {


        //ignored!
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
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public DirectedGraph<Fisher, FriendshipEdge> apply(final FishState fishState) {

        final DirectedSparseGraph<Fisher, FriendshipEdge> graph = new DirectedSparseGraph<>();


        final int numberOfEdges = (int) edges.applyAsDouble(fishState.getRandom());

        final ObservableList<Fisher> fishers = fishState.getFishers();

        final List<NetworkPredicate> predicates = computePredicates(fishState);
        mainloop:
        while (graph.getEdgeCount() < numberOfEdges) {
            final Fisher first = fishers.get(fishState.getRandom().nextInt(fishers.size()));
            final Fisher second = fishers.get(fishState.getRandom().nextInt(fishers.size()));

            final Collection<Fisher> successors = graph.getSuccessors(first);
            if (successors != null && successors.contains(second))
                continue;
            for (final NetworkPredicate predicate : predicates) {
                if (!predicate.test(first, second))
                    continue mainloop;
            }

            graph.addEdge(
                new FriendshipEdge(),
                first,
                second
            );


        }


        return graph;

    }

    /**
     * Getter for property 'edges'.
     *
     * @return Value for property 'edges'.
     */
    public DoubleParameter getEdges() {
        return edges;
    }

    /**
     * Setter for property 'edges'.
     *
     * @param edges Value to set for property 'edges'.
     */
    public void setEdges(final DoubleParameter edges) {
        this.edges = edges;
    }


}
