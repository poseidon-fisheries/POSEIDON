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

package uk.ac.ox.oxfish.geography.discretization;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The factory building squared maps
 * Created by carrknight on 1/27/17.
 */
public class SquaresMapDiscretizerFactory implements AlgorithmFactory<SquaresMapDiscretizer> {


    private DoubleParameter horizontalSplits = new FixedDoubleParameter(2);

    private DoubleParameter verticalSplits = new FixedDoubleParameter(2);


    public SquaresMapDiscretizerFactory() {
    }

    public SquaresMapDiscretizerFactory(final int horizontalSplits, final int verticalSplits) {
        this.horizontalSplits = new FixedDoubleParameter(horizontalSplits);
        this.verticalSplits = new FixedDoubleParameter(verticalSplits);
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
