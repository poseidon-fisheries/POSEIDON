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

package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.MaximumDaysDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The factory of MaximumDaysStrategy. For the factory instead of focusing on steps I focus on days
 * Created by carrknight on 6/23/15.
 */
public class MaximumStepsFactory implements AlgorithmFactory<MaximumDaysDecorator>
{

    /**
     * how many DAYS (not steps) after which the fisher refuses to fish
     */
    private DoubleParameter daysAtSea = new FixedDoubleParameter(5);


    public MaximumStepsFactory() {
    }


    public MaximumStepsFactory(double daysAtSea) {
        this.daysAtSea = new FixedDoubleParameter(daysAtSea);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MaximumDaysDecorator apply(FishState state) {

        int rounded = (int) Math.round(daysAtSea.apply(state.random));

        return new MaximumDaysDecorator(rounded);

    }

    public DoubleParameter getDaysAtSea() {
        return daysAtSea;
    }

    public void setDaysAtSea(DoubleParameter daysAtSea) {
        this.daysAtSea = daysAtSea;
    }
}
