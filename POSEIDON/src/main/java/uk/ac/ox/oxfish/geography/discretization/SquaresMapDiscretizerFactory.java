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

package uk.ac.ox.oxfish.geography.discretization;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

/**
 * The factory building squared maps Created by carrknight on 1/27/17.
 */
public class SquaresMapDiscretizerFactory implements AlgorithmFactory<SquaresMapDiscretizer> {

    private DoubleParameter horizontalSplits;

    private DoubleParameter verticalSplits;

    public SquaresMapDiscretizerFactory() {
    }

    public SquaresMapDiscretizerFactory(
        final DoubleParameter horizontalSplits,
        final DoubleParameter verticalSplits
    ) {
        this.horizontalSplits = horizontalSplits;
        this.verticalSplits = verticalSplits;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public SquaresMapDiscretizer apply(final FishState fishState) {
        return new SquaresMapDiscretizer(
            (int) verticalSplits.applyAsDouble(fishState.getRandom()),
            (int) horizontalSplits.applyAsDouble(fishState.getRandom())
        );
    }

    /**
     * Getter for property 'horizontalSplits'.
     *
     * @return Value for property 'horizontalSplits'.
     */
    public DoubleParameter getHorizontalSplits() {
        return horizontalSplits;
    }

    /**
     * Setter for property 'horizontalSplits'.
     *
     * @param horizontalSplits Value to set for property 'horizontalSplits'.
     */
    public void setHorizontalSplits(final DoubleParameter horizontalSplits) {
        this.horizontalSplits = horizontalSplits;
    }

    /**
     * Getter for property 'verticalSplits'.
     *
     * @return Value for property 'verticalSplits'.
     */
    public DoubleParameter getVerticalSplits() {
        return verticalSplits;
    }

    /**
     * Setter for property 'verticalSplits'.
     *
     * @param verticalSplits Value to set for property 'verticalSplits'.
     */
    public void setVerticalSplits(final DoubleParameter verticalSplits) {
        this.verticalSplits = verticalSplits;
    }
}
