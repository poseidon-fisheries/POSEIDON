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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * A list of strategies. All have to return true or you don't depart
 * Created by carrknight on 9/11/15.
 */
public class CompositeDepartingStrategy implements DepartingStrategy {

    private final DepartingStrategy[] strategies;

    public CompositeDepartingStrategy(DepartingStrategy... strategies) {
        this.strategies = strategies;
    }

    public List<DepartingStrategy> getStrategies() { return unmodifiableList(asList(strategies)); }

    @Override
    public void turnOff(Fisher fisher) {
        for (DepartingStrategy strategy : strategies)
            strategy.turnOff(fisher);
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        for (DepartingStrategy strategy : strategies)
            strategy.start(model, fisher);
    }

    /**
     * All the given strategies must return true for the fisher to go out
     *
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
        Fisher fisher, FishState model, MersenneTwisterFast random
    ) {
        for (DepartingStrategy strategy : strategies)
            if (!strategy.shouldFisherLeavePort(fisher, model, model.getRandom()))
                return false;
        return true;
    }

}
