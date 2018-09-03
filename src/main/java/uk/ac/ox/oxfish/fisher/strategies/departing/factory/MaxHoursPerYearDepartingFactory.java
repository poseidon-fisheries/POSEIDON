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

package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.strategies.departing.MaxHoursPerYearDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;


public class MaxHoursPerYearDepartingFactory implements AlgorithmFactory<MaxHoursPerYearDepartingStrategy>
{

    private DoubleParameter maxHoursOut = new FixedDoubleParameter(1200);


    public MaxHoursPerYearDepartingFactory() {
    }


    public MaxHoursPerYearDepartingFactory(double maxHoursOut) {
        this.maxHoursOut = new FixedDoubleParameter(maxHoursOut);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MaxHoursPerYearDepartingStrategy apply(FishState fishState) {
        return new MaxHoursPerYearDepartingStrategy(
                maxHoursOut.apply(fishState.getRandom())
        );
    }

    /**
     * Getter for property 'maxHoursOut'.
     *
     * @return Value for property 'maxHoursOut'.
     */
    public DoubleParameter getMaxHoursOut() {
        return maxHoursOut;
    }

    /**
     * Setter for property 'maxHoursOut'.
     *
     * @param maxHoursOut Value to set for property 'maxHoursOut'.
     */
    public void setMaxHoursOut(DoubleParameter maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
    }
}
