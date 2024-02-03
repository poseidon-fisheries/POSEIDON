/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ClubNetworkBuilder extends AbstractNetworkBuilder {

    private int nextClubSize;

    private Collection<Fisher> club;


    private DoubleParameter clubSize = new FixedDoubleParameter(2);

    public ClubNetworkBuilder() {
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
        final List<Fisher> fishers = new ArrayList<>(fishState.getFishers());

        for (final Fisher fisher : fishers) {
            addFisher(fisher, graph, fishState);
        }
        return graph;

    }

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

        final List<NetworkPredicate> predicates = super.computePredicates(state);


        //build new club
        if (club == null || club.size() >= nextClubSize) {
            club = new LinkedList<>();
            nextClubSize = (int) clubSize.applyAsDouble(state.getRandom());
        }
        //make sure this club is okay
        clubpredicates:
        for (final Fisher clubMember : club) {
            for (final NetworkPredicate predicate : predicates)
                if (!predicate.test(clubMember, newAddition)) {
                    club = new LinkedList<>();
                    break clubpredicates;
                }
        }

        //add yourself as friend of everybody in the club
        for (final Fisher clubMember : club) {
            currentNetwork.addEdge(
                new FriendshipEdge(),
                clubMember,
                newAddition
            );
            currentNetwork.addEdge(
                new FriendshipEdge(),
                newAddition,
                clubMember
            );

        }
        club.add(newAddition);


    }

    /**
     * Getter for property 'nextClubSize'.
     *
     * @return Value for property 'nextClubSize'.
     */
    public int getNextClubSize() {
        return nextClubSize;
    }


    /**
     * Getter for property 'clubSize'.
     *
     * @return Value for property 'clubSize'.
     */
    public DoubleParameter getClubSize() {
        return clubSize;
    }

    /**
     * Setter for property 'clubSize'.
     *
     * @param clubSize Value to set for property 'clubSize'.
     */
    public void setClubSize(final DoubleParameter clubSize) {
        this.clubSize = clubSize;
    }
}
