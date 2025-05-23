/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.MaximumDaysAYearDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class MaximumDaysAYearFactory implements AlgorithmFactory<MaximumDaysAYearDecorator> {


    private AlgorithmFactory<? extends FishingStrategy> delegate = new FishUntilFullFactory();


    private DoubleParameter maxNumberOfDaysOutPerYear = new FixedDoubleParameter(240);

    public MaximumDaysAYearFactory() {
    }

    public MaximumDaysAYearFactory(
        final int maxNumberOfDaysOutPerYear,
        final AlgorithmFactory<? extends FishingStrategy> delegate
    ) {
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
    public MaximumDaysAYearDecorator apply(final FishState state) {
        return new MaximumDaysAYearDecorator(
            delegate.apply(state),
            (int) maxNumberOfDaysOutPerYear.applyAsDouble(state.getRandom())
        );


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
        final AlgorithmFactory<? extends FishingStrategy> delegate
    ) {
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
    public void setMaxNumberOfDaysOutPerYear(final DoubleParameter maxNumberOfDaysOutPerYear) {
        this.maxNumberOfDaysOutPerYear = maxNumberOfDaysOutPerYear;
    }
}
