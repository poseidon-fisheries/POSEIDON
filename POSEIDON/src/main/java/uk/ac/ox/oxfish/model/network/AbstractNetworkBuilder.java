/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractNetworkBuilder implements NetworkBuilder {


    private List<AlgorithmFactory<NetworkPredicate>> predicates = new LinkedList<>();


    /**
     * more of a support of legacy code, we translate this back into an algorithm factory
     */
    public void addPredicate(NetworkPredicate predicate) {

        predicates.add(state -> predicate);

    }


    protected List<NetworkPredicate> computePredicates(FishState state) {

        LinkedList<NetworkPredicate> toReturn = new LinkedList<>();
        for (AlgorithmFactory<NetworkPredicate> predicate : predicates) {
            toReturn.add(predicate.apply(state));

        }

        return toReturn;


    }


    /**
     * Getter for property 'predicates'.
     *
     * @return Value for property 'predicates'.
     */
    public List<AlgorithmFactory<NetworkPredicate>> getPredicates() {
        return predicates;
    }

    /**
     * Setter for property 'predicates'.
     *
     * @param predicates Value to set for property 'predicates'.
     */
    public void setPredicates(
        List<AlgorithmFactory<NetworkPredicate>> predicates
    ) {
        this.predicates = predicates;
    }
}
