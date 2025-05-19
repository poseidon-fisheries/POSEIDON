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

import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Creates a network and manages adding more vertices to the graph
 * Created by carrknight on 12/11/15.
 */
public interface NetworkBuilder extends AlgorithmFactory<DirectedGraph<Fisher, FriendshipEdge>> {

    /**
     * adds a condition that needs to be true for two fishers to be friends.
     *
     * @param predicate the condition to add
     */
    void addPredicate(NetworkPredicate predicate);

    /**
     * this is supposed to be called not so much when initializing the network but later on if any agent is created
     * while the model is running
     */
    void addFisher(Fisher newAddition, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state);

    /**
     * remove fisher from network. This is to be used while the model is running to clear any ties
     *
     * @param toRemove       fisher to remove
     * @param currentNetwork network to modify
     * @param state
     */
    void removeFisher(Fisher toRemove, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state);

}
