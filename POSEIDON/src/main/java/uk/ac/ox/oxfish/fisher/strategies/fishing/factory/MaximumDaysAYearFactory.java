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

package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.MaximumDaysAYearDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class MaximumDaysAYearFactory implements AlgorithmFactory<MaximumDaysAYearDecorator> {


    private AlgorithmFactory<? extends FishingStrategy> delegate = new FishUntilFullFactory();


    private DoubleParameter maxNumberOfDaysOutPerYear = new FixedDoubleParameter(240);

    public MaximumDaysAYearFactory() {
    }

    public MaximumDaysAYearFactory(int maxNumberOfDaysOutPerYear,
                                   AlgorithmFactory<? extends FishingStrategy> delegate) {
        this.maxNumberOfDaysOutPerYear = new FixedDoubleParameter(maxNumberOfDaysOutPerYear);
        this.delegate = delegate;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MaximumDaysAYearDecorator apply(FishState state) {
        return new MaximumDaysAYearDecorator(delegate.apply(state),
                                             maxNumberOfDaysOutPerYear.apply(state.getRandom()).intValue());


    }


    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public AlgorithmFactory<? extends FishingStrategy> getDelegate() {
        return delegate;
    }

    /**
     * Setter for property 'delegate'.
     *
     * @param delegate Value to set for property 'delegate'.
     */
    public void setDelegate(
            AlgorithmFactory<? extends FishingStrategy> delegate) {
        this.delegate = delegate;
    }

    /**
     * Getter for property 'maxNumberOfDaysOutPerYear'.
     *
     * @return Value for property 'maxNumberOfDaysOutPerYear'.
     */
    public DoubleParameter getMaxNumberOfDaysOutPerYear() {
        return maxNumberOfDaysOutPerYear;
    }

    /**
     * Setter for property 'maxNumberOfDaysOutPerYear'.
     *
     * @param maxNumberOfDaysOutPerYear Value to set for property 'maxNumberOfDaysOutPerYear'.
     */
    public void setMaxNumberOfDaysOutPerYear(DoubleParameter maxNumberOfDaysOutPerYear) {
        this.maxNumberOfDaysOutPerYear = maxNumberOfDaysOutPerYear;
    }
}
