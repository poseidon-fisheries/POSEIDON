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
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.*;

public class ClubNetworkBuilder extends AbstractNetworkBuilder {

    private int nextClubSize;

    private Collection<Fisher> club;



    private DoubleParameter clubSize = new FixedDoubleParameter(2);

    public ClubNetworkBuilder() {
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
            Fisher newAddition, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state) {

        List<NetworkPredicate> predicates = super.computePredicates(state);



        //build new club
        if(club == null || club.size()>= nextClubSize) {
            club = new LinkedList<>();
            nextClubSize = clubSize.apply(state.getRandom()).intValue();
        }
        //make sure this club is okay
        clubpredicates:
        for(Fisher clubMember : club) {
            for (NetworkPredicate predicate : predicates)
                if (!predicate.test(clubMember, newAddition)) {
                    club = new LinkedList<>();
                    break clubpredicates;
                }
        }

        //add yourself as friend of everybody in the club
        for(Fisher clubMember : club) {
            currentNetwork.addEdge(new FriendshipEdge(),
                             clubMember,
                             newAddition);
            currentNetwork.addEdge(new FriendshipEdge(),
                             newAddition,
                             clubMember);

        }
        club.add(newAddition);




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

    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public DirectedGraph<Fisher, FriendshipEdge> apply(FishState fishState) {

        DirectedSparseGraph<Fisher, FriendshipEdge> graph = new DirectedSparseGraph<>();
        List<Fisher> fishers = new ArrayList<>(fishState.getFishers());

        for (Fisher fisher : fishers) {
            addFisher(fisher,graph,fishState);
        }
        return graph;

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
    public void setClubSize(DoubleParameter clubSize) {
        this.clubSize = clubSize;
    }
}
